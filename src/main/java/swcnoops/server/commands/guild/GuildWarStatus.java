package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildWarStatusCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

import java.util.ArrayList;

public class GuildWarStatus extends AbstractCommandAction<GuildWarStatus, GuildWarStatusCommandResult> {
    @Override
    protected GuildWarStatusCommandResult execute(GuildWarStatus arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = playerSession.getGuildSession();

        GuildWarStatusCommandResult guildWarStatusResponse =
                new GuildWarStatusCommandResult(playerSession, guildSession);

        // buff bases can not be null otherwise client crashes
        guildWarStatusResponse.buffBases = new ArrayList<>();

        // if war has finished then must set this to true otherwise squad can not start war
        guildWarStatusResponse.rewardsProcessed = true;
        // TODO
        return guildWarStatusResponse;
    }

    @Override
    protected GuildWarStatus parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarStatus.class);
    }

    @Override
    public String getAction() {
        return "guild.war.status";
    }
}
