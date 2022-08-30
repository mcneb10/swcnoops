package swcnoops.server.trigger;

import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.War;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

public class AfterCoolDownTrigger implements CommandTrigger {
    @Override
    public void process(String playerId) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerId);
        GuildSession guildSession = playerSession.getGuildSession();

        if (guildSession != null) {
            Config config = ServiceFactory.instance().getConfig();
            War war = guildSession.getCurrentWar();
            // we move it 5 mins after cool down
            war.setPrepGraceStartTime(ServiceFactory.getSystemTimeSecondsFromEpoch() - (60 * 60 * 26));
            war.setPrepEndTime(war.getPrepGraceStartTime() + config.warServerPreparationDuration);
            war.setActionGraceStartTime(war.getPrepEndTime() + config.warPlayDuration);
            war.setActionEndTime(war.getActionGraceStartTime() + config.warResultDuration);
            war.setCooldownEndTime(ServiceFactory.getSystemTimeSecondsFromEpoch() - (60 * 5));
            ServiceFactory.instance().getPlayerDatasource().saveWar(war);
        }
    }
}

