package swcnoops.server.commands.auth.preauth.response;

import swcnoops.server.requests.AbstractCommandResult;

public class GeneratePlayerCommandResult extends AbstractCommandResult {
    public String playerId;
    public String secret;
}
