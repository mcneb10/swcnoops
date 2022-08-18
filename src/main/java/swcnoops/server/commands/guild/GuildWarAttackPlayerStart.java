package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.BattleIdResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

public class GuildWarAttackPlayerStart extends AbstractCommandAction<GuildWarAttackPlayerStart, BattleIdResult> {
    private String opponentId;

    @Override
    protected BattleIdResult execute(GuildWarAttackPlayerStart arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = playerSession.getGuildSession();
        String battleId = guildSession.warAttackStart(playerSession, arguments.getOpponentId(), time);
        return new BattleIdResult(battleId);
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
