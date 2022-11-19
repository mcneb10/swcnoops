package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPlanetObjectiveResult;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.ObjectiveManager;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.ObjectiveGroup;
import swcnoops.server.session.PlayerSession;
import java.util.Map;

public class PlayerPlanetObjective extends AbstractCommandAction<PlayerPlanetObjective, PlayerPlanetObjectiveResult> {
    @Override
    protected PlayerPlanetObjectiveResult execute(PlayerPlanetObjective arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        ObjectiveManager objectiveManager = ServiceFactory.instance().getGameDataManager().getObjectiveManager();
        PlayerSettings playerSettings = playerSession.getPlayerSettings();
        Map<String, ObjectiveGroup> groups = objectiveManager.getObjectiveGroups(playerSettings.getUnlockedPlanets(),
                playerSession.getReceivedDonationsManager().getObjectForReading(),
                playerSettings.getFaction(),
                playerSettings.getHqLevel(),
                playerSettings.getTimeZoneOffset());

        PlayerPlanetObjectiveResult playerPlanetObjectiveResult = new PlayerPlanetObjectiveResult(groups);
        return playerPlanetObjectiveResult;
    }

    @Override
    protected PlayerPlanetObjective parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPlanetObjective.class);
    }

    @Override
    public String getAction() {
        return "player.planet.objective";
    }
}
