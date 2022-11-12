package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.CrateDataResult;
import swcnoops.server.json.JsonParser;

// TODO - to finish
// look at which objective is being claimed, get its reward and return
public class PlayerObjectiveClaim extends AbstractCommandAction<PlayerObjectiveClaim, CrateDataResult> {
    private String planetId;
    private String objectiveId;

    @Override
    protected CrateDataResult execute(PlayerObjectiveClaim arguments, long time) throws Exception {
        return new CrateDataResult();
    }

    @Override
    protected PlayerObjectiveClaim parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerObjectiveClaim.class);
    }

    @Override
    public String getAction() {
        return "player.objective.claim";
    }

    public String getPlanetId() {
        return planetId;
    }

    public void setPlanetId(String planetId) {
        this.planetId = planetId;
    }

    public String getObjectiveId() {
        return objectiveId;
    }

    public void setObjectiveId(String objectiveId) {
        this.objectiveId = objectiveId;
    }
}
