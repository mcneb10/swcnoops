package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.Command;
import swcnoops.server.requests.Batch;
import swcnoops.server.requests.BatchResponse;
import swcnoops.server.requests.ResponseData;
import swcnoops.server.requests.ResponseHelper;

import java.util.ArrayList;
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

    private Integer validAuthKey(String authKey) {
        Integer errorCode = null;

        String playerId = authKey.substring(0, authKey.indexOf(":"));
        String expectedKey = playerTokens.get(playerId);
        if (expectedKey != null) {
            if (!expectedKey.equals(authKey))
                errorCode = ResponseHelper.LOGIN_TIME_MISMATCH;
        } else {
            // not found a login session, either server rebooted or client is trying it on
            errorCode = ResponseHelper.STATUS_CODE_BAD_INPUT;
        }

        return errorCode;
    }

    public BatchResponse validateBatch(Batch batch) throws Exception {
        BatchResponse batchResponse = null;

        if (batch.getAuthKey() == null || batch.getAuthKey().isEmpty()) {
            if (!this.validAuthCommands(batch.getCommands())) {
                throw new Exception("Invalid commands without authKey");
            }
        } else {
            Integer errorCode = this.validAuthKey(batch.getAuthKey());
            if (errorCode != null) {
                // another device
                batchResponse = createAuthErrorResponse(batch, errorCode);
            }
        }

        return batchResponse;
    }

    private BatchResponse createAuthErrorResponse(Batch batch, Integer errorCode) {
        List<ResponseData> responseDatum = new ArrayList<>();
        for (Command command : batch.getCommands()) {
            ResponseData responseData = new ResponseData();
            responseData.requestId = command.getRequestId();
            responseData.status = errorCode;
            responseDatum.add(responseData);
        }

        return new BatchResponse(responseDatum);
    }
}
