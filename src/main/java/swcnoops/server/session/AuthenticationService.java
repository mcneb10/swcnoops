package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.Command;
import swcnoops.server.requests.Batch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticationService {
    private Map<String, String> playerTokens = new ConcurrentHashMap<>();
    public String createToken(String playerId) {
        playerTokens.put(playerId, playerId + ":" + ServiceFactory.createRandomUUID());
        return playerTokens.get(playerId);
    }

    private boolean validAuthCommands(List<Command> commands) {
        boolean allAuthCommands = false;
        for (Command command : commands) {
            if (command.getCommandAction().isAuthCommand()) {
                allAuthCommands = true;
            } else {
                allAuthCommands = false;
                break;
            }
        }

        return allAuthCommands;
    }

    private boolean validAuthKey(String authKey) {
        String playerId = authKey.substring(0, authKey.indexOf(":"));
        String expectedKey = playerTokens.get(playerId);
        return expectedKey.equals(authKey);
    }

    public void validateBatch(Batch batch) throws Exception {
        if (!ServiceFactory.instance().getConfig().validateAuthKey)
            return;

        if (batch.getAuthKey() == null || batch.getAuthKey().isEmpty()) {
            if (!this.validAuthCommands(batch.getCommands())) {
                throw new Exception("Invalid commands without authKey");
            }
        } else {
            if (!this.validAuthKey(batch.getAuthKey()))
                throw new Exception("Invalid authKey");
        }
    }
}
