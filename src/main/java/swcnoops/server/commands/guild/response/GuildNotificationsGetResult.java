package swcnoops.server.commands.guild.response;

import swcnoops.server.model.SquadNotification;
import swcnoops.server.requests.AbstractCommandResult;

import java.util.ArrayList;
import java.util.List;

public class GuildNotificationsGetResult extends AbstractCommandResult {
    private List<SquadNotification> notifications = new ArrayList<>();

    @Override
    public Object getResult() {
        return notifications;
    }

    public void addNotification(SquadNotification squadNotification) {
        this.notifications.add(squadNotification);
    }

    public List<SquadNotification> getNotifications() {
        return notifications;
    }

    public void addNotifications(List<SquadNotification> notifications) {
        this.notifications.addAll(notifications);
    }
}
