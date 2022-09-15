package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Member;
import swcnoops.server.model.SquadMsgType;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.SessionManager;

import java.util.Optional;

public class GuildLeave extends AbstractCommandAction<GuildLeave, CommandResult> {
    @Override
    protected CommandResult execute(GuildLeave arguments, long time) throws Exception {
        SessionManager sessionManager = ServiceFactory.instance().getSessionManager();
        PlayerSession playerSession = sessionManager.getPlayerSession(arguments.getPlayerId());
        playerSession.getDonatedTroops().clear();
        GuildSession oldSquad = playerSession.getGuildSession();
        if (oldSquad != null) {
            oldSquad.leave(playerSession, SquadMsgType.leave);

            Optional<Member> foundMember =
                    oldSquad.getGuildSettings().getMembers().stream().filter(a -> a.playerId.equals(playerSession.getPlayerId())).findFirst();

            if (foundMember.isPresent())
                throw new Exception("Have left Squad but can still find player Id" + playerSession.getPlayerId());
        }

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected GuildLeave parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildLeave.class);
    }

    @Override
    public String getAction() {
        return "guild.leave";
    }
}
