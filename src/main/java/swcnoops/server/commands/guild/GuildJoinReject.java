package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.SessionManager;

public class GuildJoinReject extends AbstractCommandAction<GuildJoinReject, CommandResult> {
    private String memberId;

    @Override
    protected CommandResult execute(GuildJoinReject arguments, long time) throws Exception {
        SessionManager sessionManager = ServiceFactory.instance().getSessionManager();
        PlayerSession playerSession = sessionManager.getPlayerSession(arguments.getPlayerId());
        PlayerSession memberSession = sessionManager.getPlayerSession(arguments.getMemberId());

        GuildSession guildSession = playerSession.getGuildSession();

        if (guildSession != null) {
            guildSession.joinRequestRejected(arguments.getPlayerId(), memberSession);
        }

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected GuildJoinReject parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildJoinReject.class);
    }

    @Override
    public String getAction() {
        return "guild.join.reject";
    }

    public String getMemberId() {
        return memberId;
    }
}
