package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerLoginCommandResult;
import swcnoops.server.datasource.PlayerSecret;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.GuildInfo;
import swcnoops.server.model.PlayerModel;
import swcnoops.server.session.PlayerSession;

public class PlayerIdentityGet extends AbstractCommandAction<PlayerIdentityGet, PlayerLoginCommandResult> {
    private int identityIndex;

    @Override
    protected PlayerLoginCommandResult execute(PlayerIdentityGet arguments, long time) throws Exception {
        // get primary account ID
        String primaryAccount = PlayerIdentitySwitch.getPrimaryAccount(arguments.getPlayerId());
        PlayerSecret playerSecret = ServiceFactory.instance().getPlayerDatasource().getPlayerSecret(primaryAccount);

        String playerAccountId = primaryAccount;
        if (!arguments.getPlayerId().endsWith("_1"))
            playerAccountId = playerSecret.getSecondaryAccount();

        PlayerSession playerSession = null;
        if (playerAccountId != null && !playerAccountId.isEmpty())
            playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerAccountId);

        PlayerLoginCommandResult playerLoginCommandResult = new PlayerLoginCommandResult();

        if (playerSession != null) {
            playerLoginCommandResult.playerId = playerSession.getPlayerId();
            playerLoginCommandResult.name = playerSession.getPlayerSettings().getName();
            playerLoginCommandResult.playerModel = new PlayerModel();
            playerLoginCommandResult.playerModel.faction = playerSession.getFaction();
            playerLoginCommandResult.playerModel.map = playerSession.getPlayer().getPlayerSettings().getBaseMap();
            if (playerSession.getGuildSession() != null) {
                playerLoginCommandResult.playerModel.guildInfo = new GuildInfo();
                playerLoginCommandResult.playerModel.guildInfo.guildName = playerSession.getGuildSession().getGuildName();
            }
        }

        return playerLoginCommandResult;
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
