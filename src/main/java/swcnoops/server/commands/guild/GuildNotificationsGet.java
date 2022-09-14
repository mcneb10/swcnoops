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
        playerSession.savePlayerKeepAlive();

        GuildNotificationsGetResult guildNotificationsGetResult = new GuildNotificationsGetResult();

        if (guildSession != null) {
            playerSession.setLastNotificationSince(guildSession.getGuildId(), arguments.getSince());
            List<SquadNotification> notifications = guildSession.getNotifications(arguments.getSince());
            if (notifications != null && notifications.size() > 0) {
                guildNotificationsGetResult.addNotifications(notifications);
            }
        } else {
            List<SquadNotification> notifications = playerSession.getNotifications(0);
            if (notifications != null && notifications.size() > 0) {
                guildNotificationsGetResult.addNotifications(notifications);
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

    @Override
    public boolean canAttachGuildNotifications() {
        return false;
    }
}
