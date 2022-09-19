package swcnoops.server.commands.guild.response;

import swcnoops.server.model.BuffBase;
import swcnoops.server.model.WarSquad;
import swcnoops.server.session.GuildSession;
import java.util.List;

public class GuildWarStatusCommandResult extends GuildResult {
    public long actionEndTime;
    public long actionGraceStartTime;
    public boolean actionsStarted;
    public List<BuffBase> buffBases;
    public long cooldownEndTime;
    public WarSquad guild;
    public String id;
    public String planet;
    public long prepEndTime;
    public long prepGraceStartTime;
    public boolean rewardsProcessed;
    public WarSquad rival;

    public GuildWarStatusCommandResult(GuildSession guildSession) {
        super(guildSession);
    }
}
