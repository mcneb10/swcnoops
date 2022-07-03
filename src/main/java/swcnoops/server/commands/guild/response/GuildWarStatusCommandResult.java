package swcnoops.server.commands.guild.response;

import swcnoops.server.model.BuffBas;
import swcnoops.server.model.Guild;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

import java.util.List;

public class GuildWarStatusCommandResult extends GuildResult {
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

    public GuildWarStatusCommandResult(PlayerSession playerSession, GuildSession guildSession) {
        super(playerSession.getPlayerId(), playerSession.getPlayerSettings().getName(), guildSession);
    }
}
