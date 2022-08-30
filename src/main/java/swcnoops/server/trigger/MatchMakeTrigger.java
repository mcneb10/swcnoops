package swcnoops.server.trigger;

import swcnoops.server.ServiceFactory;
import swcnoops.server.model.SquadMsgType;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.model.WarNotificationData;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.WarSession;

import static swcnoops.server.session.NotificationFactory.createNotification;

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
                WarSession warSession = ServiceFactory.instance().getSessionManager().getWarSession(warId);
                warSession.warMatched();
            }
        }
    }

    private String doMatchMake(GuildSession guildSession) {
        SquadNotification warPreparedNotification =
                createNotification(guildSession.getGuildId(), guildSession.getGuildName(), null, SquadMsgType.warPrepared);
        warPreparedNotification.setData(new WarNotificationData());
        return ServiceFactory.instance().getPlayerDatasource()
                .matchMake(guildSession.getGuildId());
    }
}
