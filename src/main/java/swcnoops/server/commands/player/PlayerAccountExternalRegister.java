package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerAccountExternalRegisterResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.PlayerIdentityInfo;
import swcnoops.server.model.PlayerModel;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerAccountExternalRegister extends AbstractCommandAction<PlayerAccountExternalRegister, PlayerAccountExternalRegisterResult> {
    private String providerId;
    private String externalAccountId;
    private String externalAccountSecurityToken;
    private boolean overrideExistingAccountRegistration;
    private String otherLinkedProviderId;

    @Override
    protected PlayerAccountExternalRegisterResult execute(PlayerAccountExternalRegister arguments, long time) throws Exception {
        final PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        PlayerAccountExternalRegisterResult result = new PlayerAccountExternalRegisterResult();
        PlayerIdentityInfo playerIdentityInfo = new PlayerIdentityInfo();

        if ("RECOVERY".equals(arguments.getProviderId())) {
            if (playerSession.getPlayer().getPlayerSecret().getMissingSecret()) {
                if (arguments.isOverrideExistingAccountRegistration()) {
                    throw new RuntimeException("Can not discard recovery account, crashing client " + arguments.getPlayerId());
                }

                result.secret = playerSession.getPlayer().getPlayerSecret().getSecret();
                result.derivedExternalAccountId = playerSession.getPlayer().getPlayerId();
                playerIdentityInfo.playerId = playerSession.getPlayer().getPlayerId();
                playerIdentityInfo.name = playerSession.getPlayer().getPlayerSettings().getName();
                // we give an empty player model to make the client think its active
                playerIdentityInfo.playerModel = new PlayerModel();
                playerIdentityInfo.playerModel.faction = playerSession.getFaction();
                result.identities.put(playerIdentityInfo.playerId, playerIdentityInfo);
                result.setReturnCode(ResponseHelper.STATUS_CODE_ALREADY_REGISTERED);
            }
        }

        return result;
    }

    @Override
    protected PlayerAccountExternalRegister parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerAccountExternalRegister.class);
    }

    @Override
    public String getAction() {
        return "player.account.external.register";
    }

    public String getProviderId() {
        return providerId;
    }

    public String getExternalAccountId() {
        return externalAccountId;
    }

    public String getExternalAccountSecurityToken() {
        return externalAccountSecurityToken;
    }

    public boolean isOverrideExistingAccountRegistration() {
        return overrideExistingAccountRegistration;
    }

    public String getOtherLinkedProviderId() {
        return otherLinkedProviderId;
    }
}
