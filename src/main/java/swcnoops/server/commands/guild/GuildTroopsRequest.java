package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.guild.response.GuildTroopsCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseData;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class GuildTroopsRequest extends AbstractCommandAction<GuildTroopsRequest, GuildTroopsCommandResult> {
    private boolean payToSkip;
    private String message;

    @Override
    protected GuildTroopsCommandResult execute(GuildTroopsRequest arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.troopsRequest(arguments.isPayToSkip(), arguments.getMessage(), time);
        return new GuildTroopsCommandResult();
    }

//    @Override
//    public ResponseData createResponse(Command command, CommandResult commandResult) {
//        return super.createResponse(command, commandResult);
//    }

    @Override
    protected GuildTroopsRequest parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildTroopsRequest.class);
    }

    @Override
    public String getAction() {
        return "guild.troops.request";
    }

    public boolean isPayToSkip() {
        return payToSkip;
    }

    public String getMessage() {
        return message;
    }
}
