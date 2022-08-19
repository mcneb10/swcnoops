package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.BattleIdResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.WarSession;

public class GuildWarAttackPlayerStart extends AbstractCommandAction<GuildWarAttackPlayerStart, BattleIdResult> {
    private String opponentId;

    @Override
    protected BattleIdResult execute(GuildWarAttackPlayerStart arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = playerSession.getGuildSession();
        WarSession warSession = ServiceFactory.instance().getSessionManager()
                .getWarSession(guildSession.getGuildSettings().getWarId());

        String battleId = null;
        if (warSession != null)
            battleId = warSession.warAttackStart(playerSession, arguments.getOpponentId());

        BattleIdResult battleIdResult;
        if (battleId != null)
            battleIdResult = new BattleIdResult(battleId);
        else
            battleIdResult = new BattleIdResult(ResponseHelper.STATUS_CODE_GUILD_WAR_PLAYER_UNDER_ATTACK);
        return battleIdResult;
    }

    @Override
    protected GuildWarAttackPlayerStart parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarAttackPlayerStart.class);
    }

    @Override
    public String getAction() {
        return "guild.war.attackPlayer.start";
    }

    public String getOpponentId() {
        return opponentId;
    }
}
