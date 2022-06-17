package swcnoops.server.commands.auth.preauth;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.auth.preauth.response.AuthPreauthGeneratePlayerWithFacebookResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.EmptyMessage;
import swcnoops.server.requests.Messages;

public class AuthPreauthGeneratePlayerWithFacebook extends AbstractCommandAction<AuthPreauthGeneratePlayerWithFacebook, AuthPreauthGeneratePlayerWithFacebookResult> {
    @Override
    protected AuthPreauthGeneratePlayerWithFacebook parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, AuthPreauthGeneratePlayerWithFacebook.class);
    }

    @Override
    protected AuthPreauthGeneratePlayerWithFacebookResult execute(AuthPreauthGeneratePlayerWithFacebook arguments) throws Exception {
        // TODO - change generate our guid and secret
        AuthPreauthGeneratePlayerWithFacebookResult generatePlayerResponse = new AuthPreauthGeneratePlayerWithFacebookResult();
        generatePlayerResponse.playerId = "2c2d4aea-7f38-11e5-a29f-069096004f69";
        generatePlayerResponse.secret = "1118035f8f4160d5606e0c1a5e101ae5";
        return generatePlayerResponse;
    }

    @Override
    protected Messages createMessage(Command command) {
        return EmptyMessage.instance;
    }

    @Override
    public String getAction() {
        return "auth.preauth.generatePlayerWithFacebook";
    }
}
