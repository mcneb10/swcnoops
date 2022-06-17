package swcnoops.server.commands.guild.response;

import swcnoops.server.model.BuffBas;
import swcnoops.server.model.Guild;
import swcnoops.server.requests.AbstractCommandResult;

import java.util.List;

public class GuildWarStatusCommandResult extends AbstractCommandResult {
    public long actionEndTime;
    public long actionGraceStartTime;
    public boolean actionsStarted;
    public List<BuffBas> buffBases;
    public long cooldownEndTime;
    public Object empireGuild;
    public Guild guild;
    public String id;
    public String planet;
    public long prepEndTime;
    public long prepGraceStartTime;
    public Object rebelsGuild;
    public boolean rewardsProcessed;
    public Guild rival;
}
