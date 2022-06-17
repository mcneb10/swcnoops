package swcnoops.server.commands.guild;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildWarStatusCommandResult;
import swcnoops.server.json.JsonParser;

public class GuildWarStatus extends AbstractCommandAction<GuildWarStatus, GuildWarStatusCommandResult> {
    @Override
    protected GuildWarStatusCommandResult execute(GuildWarStatus arguments) throws Exception {
        GuildWarStatusCommandResult guildWarStatusResponse =
                parseJsonFile("templates/GuildWarStatus.json", GuildWarStatusCommandResult.class);

        // TODO
        return guildWarStatusResponse;
    }

    @Override
    protected GuildWarStatus parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarStatus.class);
    }

    @Override
    public String getAction() {
        return "guild.war.status";
    }
}
