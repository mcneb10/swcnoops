package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

/**
 * This is called on login to get the players squad details.
 */
public class GuildGet extends AbstractCommandAction<GuildGet, SquadResult> {

    @Override
    protected SquadResult execute(GuildGet arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        String guildId = playerSession.getPlayerSettings().getGuildId();
        if (guildId == null || guildId.isEmpty())
            return null;

        // TODO - maybe move this and make it part of login or loading the players session
        GuildSession guildSession = ServiceFactory.instance().getSessionManager()
                .getGuildSession(playerSession.getPlayerSettings(), guildId);
        if (guildSession == null)
            throw new RuntimeException("Unknown guild " + guildId);

        guildSession.join(playerSession);
        SquadResult squadResult = GuildCommandAction.createSquadResult(guildSession);
        return squadResult;
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
