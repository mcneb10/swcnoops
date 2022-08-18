package swcnoops.server.commands.auth;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.TokenHelper;
import swcnoops.server.commands.player.PlayerIdentitySwitch;
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

        if (playerSecret == null)
            throw new RuntimeException("Unknown player " + arguments.getPlayerId());

        String expectedToken = TokenHelper.generateToken(message, playerSecret.getSecret());

//        if (!expectedToken.equals(requestToken))
//            throw new RuntimeException("Invalid requestToken by player " + arguments.getPlayerId());

        // we send back what they gave us
        return ResponseHelper.newStringResponse(arguments.getRequestToken(), true);
    }

    @Override
    protected Messages createMessage(Command command, CommandResult commandResult) {
        return EmptyMessage.instance;
    }

    @Override
    public boolean canAttachGuildNotifications() {
        return false;
    }
}
