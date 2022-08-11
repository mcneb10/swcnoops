package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.datasource.PlayerSecret;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

// TODO - to finish, return proper response
public class PlayerIdentityGet extends AbstractCommandAction<PlayerIdentityGet, CommandResult> {
    private int identityIndex;

    @Override
    protected CommandResult execute(PlayerIdentityGet arguments, long time) throws Exception {
        // get primary account ID
        String primaryAccount = PlayerIdentitySwitch.getPrimaryAccount(arguments.getPlayerId());
        PlayerSecret playerSecret = ServiceFactory.instance().getPlayerDatasource().getPlayerSecret(primaryAccount);

        String playerAccountId = primaryAccount;

        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerAccountId);

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerIdentityGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerIdentityGet.class);
    }

    @Override
    public String getAction() {
        return "player.identity.get";
    }

    public int getIdentityIndex() {
        return identityIndex;
    }
}
