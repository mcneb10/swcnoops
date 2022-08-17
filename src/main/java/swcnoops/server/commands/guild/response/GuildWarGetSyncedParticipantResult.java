package swcnoops.server.commands.guild.response;

import swcnoops.server.model.SquadMemberWarData;
import swcnoops.server.requests.AbstractCommandResult;

public class GuildWarGetSyncedParticipantResult extends AbstractCommandResult {
    private SquadMemberWarData squadMemberWarData;
    public GuildWarGetSyncedParticipantResult(SquadMemberWarData squadMemberWarData) {
        this.squadMemberWarData = squadMemberWarData;
    }

    @Override
    public Object getResult() {
        return this.squadMemberWarData;
    }
}
