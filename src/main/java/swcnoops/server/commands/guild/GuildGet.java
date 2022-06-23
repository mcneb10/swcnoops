package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.guild.response.GuildGetCommandResult;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseData;
import swcnoops.server.requests.ResponseHelper;

public class GuildGet extends AbstractCommandAction<GuildGet, GuildGetCommandResult> {

    @Override
    protected GuildGetCommandResult execute(GuildGet arguments, long time) throws Exception {
        GuildGetCommandResult guildGetResult =
                parseJsonFile(ServiceFactory.instance().getConfig().guildGetTemplate, GuildGetCommandResult.class);

        // TODO
//        swcSession.warId = swcSession.getPlayerSettings().currentRivalWarSquadId;
//        guildGetResponse.id = swcSession.getGuildId();
//        guildGetResponse.currentWarId = swcSession.warId;
        //guildGetResponse.members.get(1).playerId = swcSession.getPlayerId();
        //guildGetResponse.members.get(1).warParty = 0;

        return guildGetResult;
    }

    @Override
    public ResponseData createResponse(Command command, CommandResult commandResult) {
        GuildGetCommandResult guildGetCommandResult = (GuildGetCommandResult) commandResult;
        if (guildGetCommandResult.id == null) {
            commandResult = ResponseHelper.SUCCESS_NULL_COMMAND_RESULT;
        }

        return super.createResponse(command, commandResult);
    }

    @Override
    protected GuildGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildGet.class);
    }

    @Override
    public String getAction() {
        return "guild.get";
    }


}
