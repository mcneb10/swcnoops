package swcnoops.server.commands.auth;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
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

        // TODO - should check token is valid by hashing with its secret and checking we get the same result
        // for now we send back what they gave us
        return ResponseHelper.newStringResponse(arguments.getRequestToken(), true);
    }

    @Override
    protected Messages createMessage(Command command) {
        return EmptyMessage.instance;
    }
}
