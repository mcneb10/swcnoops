package swcnoops.server.commands.player.response;

import swcnoops.server.model.ObjectiveGroup;
import swcnoops.server.requests.AbstractCommandResult;

public class PlayerObjectiveForceResult extends AbstractCommandResult {
    private ObjectiveGroup objectiveGroup;

    public ObjectiveGroup getObjectiveGroup() {
        return objectiveGroup;
    }

    public void setObjectiveGroup(ObjectiveGroup objectiveGroup) {
        this.objectiveGroup = objectiveGroup;
    }

    @Override
    public Object getResult() {
        return this.getObjectiveGroup();
    }
}
