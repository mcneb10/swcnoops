package swcnoops.server.commands.guild.response;

import swcnoops.server.model.Member;
import swcnoops.server.requests.CommandResult;

public class GuildMemberResult implements CommandResult {
    private Member member;

    public GuildMemberResult(Member member) {
        this.member = member;
    }

    @Override
    public Integer getStatus() {
        return Integer.valueOf(0);
    }

    @Override
    public Object getResult() {
        return this.member;
    }
}
