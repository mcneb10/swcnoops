package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Member;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.SessionManager;

import java.util.Optional;

public class GuildJoin extends GuildCommandAction<GuildJoin, SquadResult> {
    private String guildId;

    @Override
    protected SquadResult execute(GuildJoin arguments, long time) throws Exception {
        SessionManager sessionManager = ServiceFactory.instance().getSessionManager();
        PlayerSession playerSession = sessionManager.getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = sessionManager.getGuildSession(playerSession, arguments.getGuildId());
        guildSession.join(playerSession);

        SquadResult squadResult = GuildCommandAction.createSquadResult(guildSession);

        if (squadResult != null) {
            Optional<Member> foundMember =
                    squadResult.members.stream().filter(a -> a.playerId.equals(playerSession.getPlayerId())).findFirst();

            if (!foundMember.isPresent())
                throw new Exception("Have joined Squad but could not find player Id " + playerSession.getPlayerId());
        }

        return squadResult;
    }

    @Override
    protected GuildJoin parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildJoin.class);
    }

    @Override
    public String getAction() {
        return "guild.join";
    }

    public String getGuildId() {
        return guildId;
    }
}
