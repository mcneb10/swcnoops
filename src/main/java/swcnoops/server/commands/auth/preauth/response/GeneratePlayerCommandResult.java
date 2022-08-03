package swcnoops.server.commands.auth.preauth.response;

import swcnoops.server.ServiceFactory;
import swcnoops.server.requests.AbstractCommandResult;

public class GeneratePlayerCommandResult extends AbstractCommandResult {
    public String playerId;
    public String secret;

    private GeneratePlayerCommandResult() {
    }

    static public GeneratePlayerCommandResult newInstance() {
        GeneratePlayerCommandResult generatePlayerCommandResult = new GeneratePlayerCommandResult();
        generatePlayerCommandResult.playerId = ServiceFactory.createRandomUUID();
        generatePlayerCommandResult.secret = ServiceFactory.createRandomUUID().replaceAll("-","");
        return generatePlayerCommandResult;
    }
}
