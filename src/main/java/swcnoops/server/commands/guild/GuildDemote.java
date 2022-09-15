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

public class GuildDemote extends AbstractCommandAction<GuildDemote, CommandResult> {
    private String memberId;

    @Override
    protected CommandResult execute(GuildDemote arguments, long time) throws Exception {
        SessionManager sessionManager = ServiceFactory.instance().getSessionManager();
        PlayerSession playerSession = sessionManager.getPlayerSession(arguments.getPlayerId());
        PlayerSession memberSession = sessionManager.getPlayerSession(arguments.getMemberId());
        GuildSession guildSession = playerSession.getGuildSession();
        if (guildSession != null)
            guildSession.changeSquadRole(playerSession, memberSession, SquadRole.Member, SquadMsgType.demotion);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected GuildDemote parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildDemote.class);
    }

    @Override
    public String getAction() {
        return "guild.demote";
    }

    public String getMemberId() {
        return memberId;
    }
}