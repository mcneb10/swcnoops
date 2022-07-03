package swcnoops.server.commands.guild.response;

import swcnoops.server.session.GuildSession;

public class GuildTroopsRequestCommandResult extends GuildResult {

    public GuildTroopsRequestCommandResult(GuildSession guildSession, String message, String playerId,
                                           String name)
    {
        super(playerId, name, guildSession);
        this.setSquadMessage(message);
    }


    @Override
    public Object getResult() {
        return null;
    }
}
