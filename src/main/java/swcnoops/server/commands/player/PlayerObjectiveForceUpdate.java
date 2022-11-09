package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerObjectiveForceResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.ObjectiveGroup;
import swcnoops.server.model.ObjectiveProgress;
import swcnoops.server.model.ObjectiveState;

import java.util.ArrayList;

/**
 * Called by client when it has been given invalid objectives
 */
public class PlayerObjectiveForceUpdate extends AbstractCommandAction<PlayerObjectiveForceUpdate, PlayerObjectiveForceResult> {
    private String planetId;

    @Override
    protected PlayerObjectiveForceResult execute(PlayerObjectiveForceUpdate arguments, long time) throws Exception {
        PlayerObjectiveForceResult playerObjectiveForceResult = new PlayerObjectiveForceResult();
        ObjectiveGroup objectiveGroup = new ObjectiveGroup("obj_tatooine_series22_520");
        playerObjectiveForceResult.setObjectiveGroup(objectiveGroup);
        objectiveGroup.startTime = ServiceFactory.getSystemTimeSecondsFromEpoch() - 10;
        objectiveGroup.endTime = ServiceFactory.getSystemTimeSecondsFromEpoch() + (60);
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
