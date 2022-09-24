package swcnoops.server.trigger;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.War;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

public class DeleteWarTrigger implements CommandTrigger {
    @Override
    public void process(String playerId) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerId);
        GuildSession guildSession = playerSession.getGuildSession();

        if (guildSession != null) {
            War war = guildSession.getCurrentWar();
            ServiceFactory.instance().getPlayerDatasource().deleteWarForSquads(war);
        }
    }
}
