package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.SessionManager;

public class GuildJoinRequest extends AbstractCommandAction<GuildJoinRequest, CommandResult> {
    private String guildId;
    private String message;

    @Override
    protected CommandResult execute(GuildJoinRequest arguments, long time) throws Exception {
        SessionManager sessionManager = ServiceFactory.instance().getSessionManager();
        PlayerSession playerSession = sessionManager.getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = sessionManager.getGuildSession(playerSession, arguments.getGuildId());
        guildSession.joinRequest(playerSession, arguments.getMessage());

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected GuildJoinRequest parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildJoinRequest.class);
    }

    @Override
    public String getAction() {
        return "guild.join.request";
    }

    public String getGuildId() {
        return guildId;
    }

    public String getMessage() {
        return message;
    }
}
