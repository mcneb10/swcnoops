package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerObjectiveForceResult;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.ObjectiveManager;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.ObjectiveGroup;
import swcnoops.server.session.PlayerSession;

import java.util.Map;

/**
 * Called by client when it has been given invalid objectives
 */
public class PlayerObjectiveForceUpdate extends AbstractCommandAction<PlayerObjectiveForceUpdate, PlayerObjectiveForceResult> {
    private String planetId;

    @Override
    protected PlayerObjectiveForceResult execute(PlayerObjectiveForceUpdate arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        ObjectiveManager objectiveManager = ServiceFactory.instance().getGameDataManager().getObjectiveManager();
        PlayerSettings playerSettings = playerSession.getPlayerSettings();
        Map<String, Long> receivedDonations = playerSession.getReceivedDonationsManager().getObjectForReading();
        Long donations = null;
        if (receivedDonations != null) {
            donations = receivedDonations.get(arguments.getPlanetId());
        }

        ObjectiveGroup objectiveGroup = objectiveManager.getObjectiveGroup(arguments.getPlanetId(),
                donations,
                playerSettings.getFaction(),
                playerSettings.getHqLevel(),
                playerSettings.getTimeZoneOffset());

        PlayerObjectiveForceResult playerObjectiveForceResult = new PlayerObjectiveForceResult();
        playerObjectiveForceResult.setObjectiveGroup(objectiveGroup);
        return playerObjectiveForceResult;
    }

    @Override
    protected PlayerObjectiveForceUpdate parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerObjectiveForceUpdate.class);
    }

    @Override
    public String getAction() {
        return "player.objective.forceUpdate";
    }

    public String getPlanetId() {
        return planetId;
    }

    public void setPlanetId(String planetId) {
        this.planetId = planetId;
    }
}
