package swcnoops.server.commands.guild.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.requests.AbstractCommandResult;
import swcnoops.server.session.GuildSession;

public class GuildResult extends AbstractCommandResult {
    @JsonIgnore
    final private GuildSession guildSession;
    private SquadNotification squadNotification;

    public GuildResult() {
        this(null);
    }

    public GuildResult(GuildSession guildSession)
    {
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

    public void setSquadNotification(SquadNotification squadNotification) {
        this.squadNotification = squadNotification;
    }

    public SquadNotification getSquadNotification() {
        return squadNotification;
    }
}
