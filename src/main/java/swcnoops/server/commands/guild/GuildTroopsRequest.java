package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.response.GuildTroopsRequestCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

/**
 * For this to work, the notification sent out must have a playerId that is in the same squad.
 * As there is a check by client code for this condition.
 */
public class GuildTroopsRequest extends GuildCommandAction<GuildTroopsRequest, GuildTroopsRequestCommandResult> {
    private boolean payToSkip;
    private String message;

    @Override
    protected GuildTroopsRequestCommandResult execute(GuildTroopsRequest arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.troopsRequest(arguments.isPayToSkip(), arguments.getMessage(), time);
        TroopRequestData troopRequestData = new TroopRequestData();
        troopRequestData.totalCapacity = playerSession.getSquadBuilding().getBuildingData().getStorage();
        troopRequestData.troopDonationLimit = troopRequestData.totalCapacity;
        troopRequestData.amount = troopRequestData.totalCapacity - playerSession.getDonatedTroopsTotalUnits();

        GuildTroopsRequestCommandResult guildTroopsRequestCommandResult =
                new GuildTroopsRequestCommandResult(playerSession.getGuildSession(), arguments.getMessage(),
                        "2c2d4aea-7f38-11e5-a29f-069096004f6a",
                        "SelfDonateBot");

        guildTroopsRequestCommandResult.setSquadMessage(arguments.getMessage());
        guildTroopsRequestCommandResult.setNotificationData(SquadMsgType.troopRequest, troopRequestData);
        return guildTroopsRequestCommandResult;
    }

    @Override
    protected GuildTroopsRequest parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildTroopsRequest.class);
    }

    @Override
    public String getAction() {
        return "guild.troops.request";
    }

    public boolean isPayToSkip() {
        return payToSkip;
    }

    public String getMessage() {
        return message;
    }
}
