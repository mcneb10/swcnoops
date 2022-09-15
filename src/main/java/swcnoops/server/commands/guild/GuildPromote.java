package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.SquadMsgType;
import swcnoops.server.model.SquadRole;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.SessionManager;

/**
 * On server side this will trigger processing in SquadController.OnPlayerActionSuccess.
 * Squad messages are mapped by SquadMsgUtils.GenerateMessageFromNotifObject use this to work out what the
 * notifications need.
 */
public class GuildPromote extends AbstractCommandAction<GuildPromote, CommandResult> {
    private String memberId;

    @Override
    protected CommandResult execute(GuildPromote arguments, long time) throws Exception {
        SessionManager sessionManager = ServiceFactory.instance().getSessionManager();
        PlayerSession playerSession = sessionManager.getPlayerSession(arguments.getPlayerId());
        PlayerSession memberSession = sessionManager.getPlayerSession(arguments.getMemberId());
        GuildSession guildSession = playerSession.getGuildSession();
        if (guildSession != null)
            guildSession.changeSquadRole(playerSession, memberSession, SquadRole.Officer, SquadMsgType.promotion);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected GuildPromote parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildPromote.class);
    }

    @Override
    public String getAction() {
        return "guild.promote";
    }

    public String getMemberId() {
        return memberId;
    }
}
