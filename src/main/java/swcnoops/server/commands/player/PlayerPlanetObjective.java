package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPlanetObjectiveResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.ObjectiveGroup;

// TODO - to finish to provide planet objectives
// tied to the code PlanetDetailsLargeObjectivesViewModule.RefreshScreenForPlanetChange
// we use a fake planet and fake objective so it does not crash the screen
public class PlayerPlanetObjective extends AbstractCommandAction<PlayerPlanetObjective, PlayerPlanetObjectiveResult> {
    @Override
    protected PlayerPlanetObjectiveResult execute(PlayerPlanetObjective arguments, long time) throws Exception {
        PlayerPlanetObjectiveResult playerPlanetObjectiveResult = new PlayerPlanetObjectiveResult();
        playerPlanetObjectiveResult.getGroups().put("planetBoo", new ObjectiveGroup("obj_anh40_dbl_tatooine_s"));
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
