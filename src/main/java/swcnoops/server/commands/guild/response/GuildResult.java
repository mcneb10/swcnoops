package swcnoops.server.commands.guild.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.model.SquadMsgType;
import swcnoops.server.model.SquadNotificationData;
import swcnoops.server.requests.AbstractCommandResult;
import swcnoops.server.session.GuildSession;

public class GuildResult extends AbstractCommandResult {
    @JsonIgnore
    final private GuildSession guildSession;
    @JsonIgnore
    final private String playerId;
    @JsonIgnore
    final private String playerName;
    @JsonIgnore
    private SquadMsgType squadMsgType;
    @JsonIgnore
    private String squadMessage;
    @JsonIgnore
    private SquadNotificationData notificationData;

    public GuildResult() {
        this(null, null, null);
    }

    public GuildResult(String playerId, String playerName, GuildSession guildSession)
    {
        this.playerId = playerId;
        this.playerName = playerName;
        this.guildSession = guildSession;
    }

    @JsonIgnore
    public String getGuildId() {
        if (this.guildSession == null)
            return null;

        return this.guildSession.getGuildId();
    }

    @JsonIgnore
    public String getGuildName() {
        if (this.guildSession == null)
            return null;

        return this.guildSession.getGuildName();
    }

    @JsonIgnore
    public String getPlayerId() {
        return playerId;
    }

    @JsonIgnore
    public String getPlayerName() {
        return playerName;
    }

    public SquadMsgType getSquadMsgType() {
        return squadMsgType;
    }

    public String getSquadMessage() {
        return squadMessage;
    }

    public void setSquadMessage(String squadMessage) {
        this.squadMessage = squadMessage;
    }

    public SquadNotificationData getNotificationData() {
        return notificationData;
    }

    public void setNotificationData(SquadMsgType squadMsgType, SquadNotificationData notificationData) {
        this.squadMsgType = squadMsgType;
        this.notificationData = notificationData;
    }
}
