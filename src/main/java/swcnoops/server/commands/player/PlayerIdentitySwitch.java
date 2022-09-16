package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerIdentitySwitchResult;
import swcnoops.server.commands.player.response.PlayerLoginCommandResult;
import swcnoops.server.datasource.PlayerSecret;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.FactionType;
import swcnoops.server.session.PlayerSession;

public class PlayerIdentitySwitch extends AbstractCommandAction<PlayerIdentitySwitch, PlayerIdentitySwitchResult> {
    @Override
    protected PlayerIdentitySwitchResult execute(PlayerIdentitySwitch arguments, long time) throws Exception {
        String playerId = arguments.getPlayerId();

        String primaryId = getPrimaryAccount(playerId);

        PlayerSecret playerSecret = ServiceFactory.instance().getPlayerDatasource().getPlayerSecret(primaryId);
        if (playerSecret.getSecondaryAccount() == null || playerSecret.getSecondaryAccount().isEmpty()) {
            PlayerSession primaryPlayerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(primaryId);
            FactionType otherFaction = primaryPlayerSession.getFaction() == FactionType.empire ? FactionType.rebel : FactionType.empire;

            PlayerLoginCommandResult factionFlipTemplate =
                    ServiceFactory.instance().getJsonParser()
                            .toObjectFromResource("templates/factionFlipTemplate" + otherFaction.getNameForLookup() + ".json",
                                    PlayerLoginCommandResult.class);

            ServiceFactory.instance().getPlayerDatasource()
                    .newPlayer(primaryId + "_1", playerSecret.getSecret(),
                            factionFlipTemplate.playerModel,
                            factionFlipTemplate.sharedPrefs,
                            null);
        }

        String otherAccount = getOtherAccount(playerId);

        return new PlayerIdentitySwitchResult(otherAccount);
    }

    final static public String getOtherAccount(String playerId) {
        String otherAccount = playerId;
        if (otherAccount.endsWith("_1"))
            otherAccount = otherAccount.substring(0, playerId.length() - 2);
        else
            otherAccount = otherAccount + "_1";

        return otherAccount;
    }

    final static public String getPrimaryAccount(String playerId) {
        String primaryId = playerId;
        if (primaryId.endsWith("_1"))
            primaryId = primaryId.substring(0, primaryId.length() - 2);

        return primaryId;
    }

    @Override
    protected PlayerIdentitySwitch parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerIdentitySwitch.class);
    }

    @Override
    public String getAction() {
        return "player.identity.switch";
    }

    @Override
    public boolean canAttachGuildNotifications() {
        return false;
    }
}
