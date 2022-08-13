package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.response.GuildTroopsDonateCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

import java.util.Map;

/**
 * TODO - finish this properly, for now it is self donating
 */
public class GuildTroopsDonate extends GuildCommandAction<GuildTroopsDonate, GuildTroopsDonateCommandResult> {
    private Map<String, Integer> troopsDonated;
    private String recipientId;
    private String requestId;

    @Override
    protected GuildTroopsDonateCommandResult execute(GuildTroopsDonate arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        SquadNotification squadNotification = playerSession.troopsDonate(arguments.getTroopsDonated(),
                arguments.getRequestId(), arguments.getRecipientId(), time);

        // TODO - do we need to deal with any race conditions when multiple players
        // are trying to donate to the same player, a problem to be solved when doing squad support

        // not sure about this yet, this probably controls the SC space that the client has on screen
        TroopDonationProgress troopDonationProgress = null;
        GuildTroopsDonateCommandResult guildTroopsDonateCommandResult =
                new GuildTroopsDonateCommandResult(playerSession.getGuildSession(), arguments.getTroopsDonated(),
                        false, troopDonationProgress);

        guildTroopsDonateCommandResult.setSquadNotification(squadNotification);
        return guildTroopsDonateCommandResult;
    }

    @Override
    protected GuildTroopsDonate parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildTroopsDonate.class);
    }

    @Override
    public String getAction() {
        return "guild.troops.donate";
    }

    public Map<String, Integer> getTroopsDonated() {
        return troopsDonated;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getRequestId() {
        return requestId;
    }
}
