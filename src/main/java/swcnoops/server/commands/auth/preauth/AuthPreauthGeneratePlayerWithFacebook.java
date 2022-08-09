package swcnoops.server.commands.auth.preauth;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.auth.preauth.response.GeneratePlayerCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.EmptyMessage;
import swcnoops.server.requests.Messages;

public class AuthPreauthGeneratePlayerWithFacebook extends AbstractCommandAction<AuthPreauthGeneratePlayerWithFacebook, GeneratePlayerCommandResult> {
    @Override
    protected AuthPreauthGeneratePlayerWithFacebook parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, AuthPreauthGeneratePlayerWithFacebook.class);
    }

    @Override
    protected GeneratePlayerCommandResult execute(AuthPreauthGeneratePlayerWithFacebook arguments, long time) throws Exception {
        throw new UnsupportedOperationException("");
    }

    @Override
    protected Messages createMessage(Command command, GeneratePlayerCommandResult commandResult) {
        return EmptyMessage.instance;
    }

    @Override
    public String getAction() {
        return "auth.preauth.generatePlayerWithFacebook";
    }
}
