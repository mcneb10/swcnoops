package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.guild.response.GuildWarGetParticipantCommandResult;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseData;
import swcnoops.server.requests.ResponseHelper;

public class GuildWarGetParticipant extends AbstractCommandAction<GuildWarGetParticipant, GuildWarGetParticipantCommandResult>
{
    @Override
    public String getAction() {
        return "guild.war.getParticipant";
    }

    @Override
    protected GuildWarGetParticipantCommandResult execute(GuildWarGetParticipant arguments) throws Exception {
        GuildWarGetParticipantCommandResult result =
                parseJsonFile(ServiceFactory.instance().getConfig().guildWarGetParticipantTemplate, GuildWarGetParticipantCommandResult.class);
        result.id = arguments.getPlayerId();
        return result;
    }

    @Override
    public ResponseData createResponse(Command command, CommandResult commandResult) {
        GuildWarGetParticipantCommandResult result = (GuildWarGetParticipantCommandResult) commandResult;

        if (result.id == null) {
            commandResult = ResponseHelper.SUCCESS_NULL_COMMAND_RESULT;
        }
        return super.createResponse(command, commandResult);
    }

    @Override
    protected GuildWarGetParticipant parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarGetParticipant.class);
    }
}
