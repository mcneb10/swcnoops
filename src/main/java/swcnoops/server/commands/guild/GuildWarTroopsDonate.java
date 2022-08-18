package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildTroopsDonateResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.TroopDonationProgress;
import swcnoops.server.session.PlayerSession;

import java.util.Map;

public class GuildWarTroopsDonate extends AbstractCommandAction<GuildWarTroopsDonate, GuildTroopsDonateResult> {
    private Map<String, Integer> troopsDonated;
    private String recipientId;
    private String requestId;

    @Override
    protected GuildTroopsDonateResult execute(GuildWarTroopsDonate arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        Map<String, Integer> donatedTroops = arguments.getTroopsDonated();
        boolean forWar = true;
        TroopDonationResult troopDonationResult = playerSession.troopsDonate(donatedTroops,
                arguments.getRequestId(), arguments.getRecipientId(), forWar, time);

        TroopDonationProgress troopDonationProgress = null;
        GuildTroopsDonateResult guildTroopsDonateCommandResult =
                new GuildTroopsDonateResult(playerSession.getGuildSession(), troopDonationResult.getDonatedTroops(),
                        false, troopDonationProgress);

        guildTroopsDonateCommandResult.setSquadNotification(troopDonationResult.getSquadNotification());
        return guildTroopsDonateCommandResult;
    }

    @Override
    protected GuildWarTroopsDonate parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarTroopsDonate.class);
    }

    @Override
    public String getAction() {
        return "guild.war.troops.donate";
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
