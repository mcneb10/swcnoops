package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerLoginCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerAccountRecover extends AbstractCommandAction<PlayerAccountRecover, CommandResult> {
    @Override
    protected CommandResult execute(PlayerAccountRecover arguments, long time) throws Exception {
        final PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        if (playerSession.getPlayer().isMissingSecret()) {
            ServiceFactory.instance().getSessionManager().removePlayerSession(playerSession.getPlayerId());
            ServiceFactory.instance().getPlayerDatasource().removeMissingSecret(playerSession.getPlayerId());

            // replace with chooseFaction stage
            PlayerLoginCommandResult factionFlipTemplate =
                    ServiceFactory.instance().getJsonParser()
                            .toObjectFromResource("templates/chooseFaction.json",
                                    PlayerLoginCommandResult.class);

            PlayerSession otherSession = ServiceFactory.instance().getSessionManager()
                    .getPlayerSession(arguments.getPlayerId(), factionFlipTemplate.playerModel);
            otherSession.getPlayerSettings().getSharedPreferences().putAll(factionFlipTemplate.sharedPrefs);
            otherSession.savePlayerSession();
        }

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerAccountRecover parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerAccountRecover.class);
    }

    @Override
    public String getAction() {
        return "player.account.recover";
    }
}
