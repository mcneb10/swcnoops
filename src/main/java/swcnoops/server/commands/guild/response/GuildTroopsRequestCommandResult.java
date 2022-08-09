package swcnoops.server.commands.guild.response;

import swcnoops.server.session.GuildSession;

public class GuildTroopsRequestCommandResult extends GuildResult {

    public GuildTroopsRequestCommandResult(GuildSession guildSession)
    {
        super(guildSession);
    }


    @Override
    public Object getResult() {
        return null;
    }
}
