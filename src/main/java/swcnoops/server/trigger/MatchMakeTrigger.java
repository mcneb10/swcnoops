package swcnoops.server.trigger;

import swcnoops.server.ServiceFactory;
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
                guildSession.warMatched(warId);
            }
        }
    }

    private String doMatchMake(GuildSession guildSession) {
        return ServiceFactory.instance().getPlayerDatasource().matchMake(guildSession.getGuildId());
    }
}
