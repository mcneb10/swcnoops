package swcnoops.server.commands.guild.response;

import swcnoops.server.model.SquadMemberWarData;
import swcnoops.server.requests.AbstractCommandResult;

public class GuildWarGetParticipantResult extends AbstractCommandResult {
    private SquadMemberWarData squadMemberWarData;
    public GuildWarGetParticipantResult(SquadMemberWarData squadMemberWarData) {
        this.squadMemberWarData = squadMemberWarData;
    }

    @Override
    public Object getResult() {
        return this.squadMemberWarData;
    }
}
