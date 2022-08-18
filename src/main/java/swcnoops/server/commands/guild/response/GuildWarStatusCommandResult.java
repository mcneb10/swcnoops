package swcnoops.server.commands.guild.response;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.War;
import swcnoops.server.model.BuffBase;
import swcnoops.server.model.SquadMemberWarData;
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

    // TODO - need to move this logic out
    public void inititalise(War war, List<SquadMemberWarData> warParticipants1,
                            List<SquadMemberWarData> warParticipants2, long time)
    {
        GuildSession guildSession = this.getGuildSession();
        if (guildSession != null) {
            if (war != null) {
                this.id = war.getWarId();
                this.prepGraceStartTime = war.getPrepGraceStartTime();
                this.prepEndTime = war.getPrepEndTime();
                this.actionGraceStartTime = war.getActionGraceStartTime();
                this.actionEndTime = war.getActionEndTime();
                this.cooldownEndTime = war.getCooldownEndTime();

                GuildSession guildSession1 = ServiceFactory.instance().getSessionManager()
                        .getGuildSession(war.getSquadIdA());

                this.guild = WarSquad.map(guildSession1, warParticipants1);

                GuildSession guildSession2 = ServiceFactory.instance().getSessionManager()
                        .getGuildSession(war.getSquadIdB());
                this.rival = WarSquad.map(guildSession2, warParticipants2);

                // we want the player's guild to be on the left of the war screen
                if (!guildSession.getGuildId().equals(this.guild.guildId)) {
                    WarSquad temp = this.guild;
                    this.guild = this.rival;
                    this.rival = temp;
                }
            }
        }
    }
}
