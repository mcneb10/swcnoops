package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.BattleIdResult;
import swcnoops.server.datasource.AttackDetail;
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
        BattleIdResult battleIdResult;

        GuildSession guildSession = playerSession.getGuildSession();
        if (guildSession != null) {
            WarSession warSession = ServiceFactory.instance().getSessionManager()
                    .getWarSession(guildSession.getWarId());

            if (warSession != null) {
                AttackDetail attackDetail = warSession.warAttackStart(playerSession, arguments.getOpponentId(), time);

                if (attackDetail != null && attackDetail.getBattleId() != null)
                    battleIdResult = new BattleIdResult(attackDetail.getBattleId());
                else
                    battleIdResult = new BattleIdResult(attackDetail.getReturnCode());
            } else {
                // TODO - might not be the correct error code to send back but if no war session, not sure what to send
                battleIdResult = new BattleIdResult(ResponseHelper.STATUS_CODE_GUILD_WAR_EXPIRED);
            }
        } else {
            battleIdResult = new BattleIdResult(ResponseHelper.STATUS_CODE_NOT_IN_GUILD);
        }

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
