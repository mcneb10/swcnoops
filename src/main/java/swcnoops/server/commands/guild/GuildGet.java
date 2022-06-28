package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.guild.response.GuildGetCommandResult;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

import java.util.ArrayList;

public class GuildGet extends AbstractCommandAction<GuildGet, GuildGetCommandResult> {

    @Override
    protected GuildGetCommandResult execute(GuildGet arguments, long time) throws Exception {
        GuildGetCommandResult guildGetResult =
                parseJsonFile(ServiceFactory.instance().getConfig().guildGetTemplate, GuildGetCommandResult.class);

        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        // TODO - get guild for the player
        String guildId = guildGetResult.id;
        String guildName = guildGetResult.name;

        GuildSession guildSession = ServiceFactory.instance().getSessionManager().getGuildSession(guildId, guildName);
        if (guildSession == null)
            throw new RuntimeException("Unknown guild " + guildId);

        guildSession.join(playerSession);
        mapToResponse(guildGetResult, playerSession);

        // TODO
//        swcSession.warId = swcSession.getPlayerSettings().currentRivalWarSquadId;
//        guildGetResponse.id = swcSession.getGuildId();
//        guildGetResponse.currentWarId = swcSession.warId;
        //guildGetResponse.members.get(1).playerId = swcSession.getPlayerId();
        //guildGetResponse.members.get(1).warParty = 0;

        return guildGetResult;
    }

    private void mapToResponse(GuildGetCommandResult guildGetResult, PlayerSession playerSession) {
        guildGetResult.warSignUpTime = null;
        guildGetResult.warRating = null;
        guildGetResult.warHistory = new ArrayList<>();
        guildGetResult.lastPerkNotif = 0;
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
