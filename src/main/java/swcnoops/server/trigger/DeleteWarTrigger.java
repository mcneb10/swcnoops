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
            GuildSession guildSession1 = ServiceFactory.instance().getSessionManager().getGuildSession(war.getSquadIdA());
            GuildSession guildSession2 = ServiceFactory.instance().getSessionManager().getGuildSession(war.getSquadIdB());
            guildSession1.getGuildSettings().setWarId(null);
            guildSession2.getGuildSettings().setWarId(null);
            ServiceFactory.instance().getPlayerDatasource().deleteWarForSquads(war);
        }
    }
}
