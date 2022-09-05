package swcnoops.server.trigger;

import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.War;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.WarSession;

public class EndWarTrigger implements CommandTrigger {
    @Override
    public void process(String playerId) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerId);
        GuildSession guildSession = playerSession.getGuildSession();

        if (guildSession != null) {
            Config config = ServiceFactory.instance().getConfig();
            War war = guildSession.getCurrentWar();
            // we move it 2 hours after end of war
            war.setPrepGraceStartTime(ServiceFactory.getSystemTimeSecondsFromEpoch() - (60 * 60 * 5));
            war.setPrepEndTime(war.getPrepGraceStartTime() + config.warServerPreparationDuration);
            war.setActionGraceStartTime(ServiceFactory.getSystemTimeSecondsFromEpoch() - (60 * 60 * 2));
            war.setActionEndTime(war.getActionGraceStartTime() + config.warResultDuration);
            war.setCooldownEndTime(war.getActionEndTime() + config.warCoolDownDuration);
            war.setProcessedEndTime(0);
            ServiceFactory.instance().getPlayerDatasource().saveWar(war);
            ServiceFactory.instance().getPlayerDatasource().resetWarPartyForParticipants(war.getWarId());
            WarSession warSession = ServiceFactory.instance().getSessionManager().getWarSession(war.getWarId());
            warSession.setDirty();
            GuildSession guildSession1 = ServiceFactory.instance().getSessionManager().getGuildSession(warSession.getGuildIdA());
            guildSession1.getGuildSettings().setDirty();
            GuildSession guildSession2 = ServiceFactory.instance().getSessionManager().getGuildSession(warSession.getGuildIdB());
            guildSession2.getGuildSettings().setDirty();
        }
    }
}
