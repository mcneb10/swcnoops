package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.response.BattleIdResult;
import swcnoops.server.commands.player.PlayerBattleComplete;
import swcnoops.server.datasource.AttackDetail;
import swcnoops.server.datasource.DefendingWarParticipant;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.BattleReplay;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.WarSession;

public class GuildWarAttackPlayerComplete extends PlayerBattleComplete<GuildWarAttackPlayerComplete, CommandResult> {
    @Override
    protected CommandResult execute(GuildWarAttackPlayerComplete arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());

        GuildSession guildSession = playerSession.getGuildSession();
        AttackDetail attackDetail = null;
        if (guildSession != null) {
            WarSession warSession = ServiceFactory.instance().getSessionManager()
                    .getWarSession(guildSession.getGuildSettings().getWarId());

            DefendingWarParticipant defendingWarParticipant = ServiceFactory.instance().getPlayerDatasource()
                    .getDefendingWarParticipantByBattleId(arguments.getBattleId());

            PlayerSession defenderSession = ServiceFactory.instance().getSessionManager()
                    .getPlayerSession(defendingWarParticipant.getPlayerId());

            BattleReplay battleReplay = BattleReplay.map(arguments, playerSession, defenderSession, time);
            attackDetail = warSession.warAttackComplete(playerSession, defenderSession, battleReplay,
                    defendingWarParticipant, time);
        }

        return new BattleIdResult(attackDetail.getReturnCode());
    }

    @Override
    protected GuildWarAttackPlayerComplete parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarAttackPlayerComplete.class);
    }

    @Override
    public String getAction() {
        return "guild.war.attackPlayer.complete";
    }
}
