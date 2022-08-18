package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.response.GuildTroopsDonateResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;
import java.util.Map;

/**
 * TODO - finish this properly, for now it is self donating
 */
public class GuildTroopsDonate extends GuildCommandAction<GuildTroopsDonate, GuildTroopsDonateResult> {
    private Map<String, Integer> troopsDonated;
    private String recipientId;
    private String requestId;

    @Override
    protected GuildTroopsDonateResult execute(GuildTroopsDonate arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        Map<String, Integer> donatedTroops = arguments.getTroopsDonated();

        TroopDonationResult troopDonationResult = playerSession.troopsDonate(donatedTroops,
                arguments.getRequestId(), arguments.getRecipientId(), false, time);

        // TODO - do we need to deal with any race conditions when multiple players
        // are trying to donate to the same player, a problem to be solved when doing squad support

        // not sure about this yet, this probably controls the SC space that the client has on screen
        TroopDonationProgress troopDonationProgress = null;
        GuildTroopsDonateResult guildTroopsDonateCommandResult =
                new GuildTroopsDonateResult(playerSession.getGuildSession(), troopDonationResult.getDonatedTroops(),
                        false, troopDonationProgress);

        guildTroopsDonateCommandResult.setSquadNotification(troopDonationResult.getSquadNotification());
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
