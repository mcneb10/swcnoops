package swcnoops.server.commands.auth;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.TokenHelper;
import swcnoops.server.commands.auth.preauth.response.GeneratePlayerCommandResult;
import swcnoops.server.commands.player.PlayerIdentitySwitch;
import swcnoops.server.commands.player.response.PlayerLoginCommandResult;
import swcnoops.server.datasource.PlayerSecret;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.EmptyMessage;
import swcnoops.server.requests.Messages;
import swcnoops.server.requests.ResponseHelper;

import java.util.Base64;

public class GetAuthToken extends AbstractCommandAction<GetAuthToken, CommandResult> {
    private String requestToken;
    private String deviceType;
    public String getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    @Override
    final public String getAction() {
        return "auth.getAuthToken";
    }

    @Override
    protected GetAuthToken parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GetAuthToken.class);
    }

    @Override
    protected CommandResult execute(GetAuthToken arguments, long time) throws Exception {
        byte[] a = Base64.getDecoder().decode(arguments.getRequestToken());
        String requestToken = new String(a);

        String message = requestToken.substring(requestToken.indexOf("{"));
        String primaryAccount = PlayerIdentitySwitch.getPrimaryAccount(arguments.getPlayerId());
        PlayerSecret playerSecret = ServiceFactory.instance().getPlayerDatasource().getPlayerSecret(primaryAccount);

        if (playerSecret == null) {
            if (!ServiceFactory.instance().getConfig().handleMissingAccounts)
                throw new RuntimeException("Unknown player " + arguments.getPlayerId());

            GeneratePlayerCommandResult generatePlayerResponse = GeneratePlayerCommandResult.newInstance();
            PlayerLoginCommandResult template = loadPlayerTemplate();
            ServiceFactory.instance().getPlayerDatasource()
                    .newPlayerWithMissingSecret(arguments.getPlayerId(), generatePlayerResponse.secret,
                            template.playerModel, template.sharedPrefs, "new");
            playerSecret = ServiceFactory.instance().getPlayerDatasource().getPlayerSecret(arguments.getPlayerId());
        }

        String expectedToken = TokenHelper.generateToken(message, playerSecret.getSecret());

        if (!playerSecret.getMissingSecret() && !expectedToken.equals(requestToken)) {
            throw new RuntimeException("Invalid requestToken by player " + arguments.getPlayerId());
        }

        // we send back a token
        String token = ServiceFactory.instance().getAuthenticationService().createToken(arguments.getPlayerId());
        return ResponseHelper.newStringResponse(token, true);
    }

    private PlayerLoginCommandResult loadPlayerTemplate() throws Exception {
        PlayerLoginCommandResult response = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().playerLoginTemplate, PlayerLoginCommandResult.class);
        return response;
    }

    @Override
    protected Messages createMessage(Command command, CommandResult commandResult) {
        return EmptyMessage.instance;
    }

    @Override
    public boolean canAttachGuildNotifications() {
        return false;
    }

    @Override
    public boolean isAuthCommand() {
        return true;
    }
}
