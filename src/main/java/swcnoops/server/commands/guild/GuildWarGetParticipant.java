package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.guild.response.GuildWarGetParticipantCommandResult;
public class GuildWarGetParticipant extends AbstractCommandAction<GuildWarGetParticipant, GuildWarGetParticipantCommandResult>
{
    @Override
    public String getAction() {
        return "guild.war.getParticipant";
    }

    @Override
    protected GuildWarGetParticipantCommandResult execute(GuildWarGetParticipant arguments, long time) throws Exception {
        // TODO - not complete
        GuildWarGetParticipantCommandResult result =
                parseJsonFile(ServiceFactory.instance().getConfig().guildWarGetParticipantTemplate, GuildWarGetParticipantCommandResult.class);
        result.id = arguments.getPlayerId();
        return result;
    }

    @Override
    protected GuildWarGetParticipant parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarGetParticipant.class);
    }
}
