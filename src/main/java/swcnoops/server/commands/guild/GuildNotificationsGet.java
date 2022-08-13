package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildNotificationsGetResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.SessionManager;

import java.util.List;

public class GuildNotificationsGet extends AbstractCommandAction<GuildNotificationsGet, GuildNotificationsGetResult> {
    private long since;
    private String battleVersion;

    @Override
    protected GuildNotificationsGetResult execute(GuildNotificationsGet arguments, long time) throws Exception {
        SessionManager sessionManager = ServiceFactory.instance().getSessionManager();
        PlayerSession playerSession = sessionManager.getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = playerSession.getGuildSession();

        GuildNotificationsGetResult guildNotificationsGetResult = new GuildNotificationsGetResult();

        if (guildSession != null) {
            boolean changedGuild = playerSession.setLastNotificationSince(guildSession.getGuildId(), arguments.getSince());

            // only on start or a change of guild do we send the notification in this command
            if (arguments.getSince() == 0 || changedGuild) {
                List<SquadNotification> notifications = guildSession.getNotificationsSince(arguments.getSince());
                if (notifications != null && notifications.size() > 0) {
                    guildNotificationsGetResult.addNotifications(notifications);
                }
            }
        }

        return guildNotificationsGetResult;
    }

    @Override
    protected GuildNotificationsGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildNotificationsGet.class);
    }

    @Override
    public String getAction() {
        return "guild.notifications.get";
    }

    public long getSince() {
        return since;
    }

    public String getBattleVersion() {
        return battleVersion;
    }
}
