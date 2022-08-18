package swcnoops.server.trigger;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.War;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

/**
 * To simulate match making for the players squad
 */
public class MatchMakeTrigger implements CommandTrigger {
    @Override
    public void process(String playerId) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerId);
        GuildSession guildSession = playerSession.getGuildSession();

        if (guildSession != null) {
            String warId = doMatchMake(guildSession);
            if (warId != null) {
                // have to set the warId for current war to be retrievable
                guildSession.getGuildSettings().setWarId(warId);
                War war = guildSession.getCurrentWar();

                GuildSession guildSession1 = ServiceFactory.instance().getSessionManager().getGuildSession(war.getSquadIdA());
                GuildSession guildSession2 = ServiceFactory.instance().getSessionManager().getGuildSession(war.getSquadIdB());

                guildSession1.getGuildSettings().setWarId(warId);
                guildSession1.warMatched(warId);
                guildSession2.getGuildSettings().setWarId(warId);
                guildSession2.warMatched(warId);
            }
        }
    }

    private String doMatchMake(GuildSession guildSession) {
        return ServiceFactory.instance().getPlayerDatasource().matchMake(guildSession.getGuildId());
    }
}
