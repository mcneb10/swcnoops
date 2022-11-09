package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPlanetObjectiveResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.ObjectiveGroup;
import swcnoops.server.model.ObjectiveProgress;
import swcnoops.server.model.ObjectiveState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// TODO - to finish to provide planet objectives
// tied to the code PlanetDetailsLargeObjectivesViewModule.RefreshScreenForPlanetChange
public class PlayerPlanetObjective extends AbstractCommandAction<PlayerPlanetObjective, PlayerPlanetObjectiveResult> {
    @Override
    protected PlayerPlanetObjectiveResult execute(PlayerPlanetObjective arguments, long time) throws Exception {
        PlayerPlanetObjectiveResult playerPlanetObjectiveResult = new PlayerPlanetObjectiveResult();

        Map<String, ObjectiveGroup> groupMap = new HashMap<>();
        ObjectiveGroup objectiveGroup = new ObjectiveGroup("obj_tatooine_series22_518");
        objectiveGroup.startTime = ServiceFactory.getSystemTimeSecondsFromEpoch() - 10;
        objectiveGroup.endTime = ServiceFactory.getSystemTimeSecondsFromEpoch() + (30);
        objectiveGroup.graceTime = objectiveGroup.endTime;
        objectiveGroup.progress = new ArrayList<>();
        ObjectiveProgress objectiveProgress = new ObjectiveProgress();
        objectiveGroup.progress.add(objectiveProgress);
        objectiveProgress.uid = "obj_donate_social_e";
        objectiveProgress.claimAttempt = false;
        objectiveProgress.count = 0;
        objectiveProgress.hq = 3;
        objectiveProgress.target = 9;
        objectiveProgress.planetId = "planet1";
        objectiveProgress.state = ObjectiveState.active;

        objectiveProgress = new ObjectiveProgress();
        objectiveGroup.progress.add(objectiveProgress);
        objectiveProgress.uid = "obj_donate_social_e";
        objectiveProgress.claimAttempt = false;
        objectiveProgress.count = 0;
        objectiveProgress.hq = 3;
        objectiveProgress.target = 8;
        objectiveProgress.planetId = "planet1";
        objectiveProgress.state = ObjectiveState.active;

        objectiveProgress = new ObjectiveProgress();
        objectiveGroup.progress.add(objectiveProgress);
        objectiveProgress.uid = "obj_train_soldier_easy_r";
        objectiveProgress.claimAttempt = false;
        objectiveProgress.count = 0;
        objectiveProgress.hq = 3;
        objectiveProgress.target = 1;
        objectiveProgress.planetId = "planet1";
        objectiveProgress.state = ObjectiveState.active;

        playerPlanetObjectiveResult.getGroups().put("planet1",objectiveGroup);
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
