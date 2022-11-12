package swcnoops.server.commands.player.response;

import swcnoops.server.model.ObjectiveGroup;
import swcnoops.server.requests.AbstractCommandResult;
import java.util.Map;

public class PlayerPlanetObjectiveResult extends AbstractCommandResult {
    private Map<String, ObjectiveGroup> groups;

    public PlayerPlanetObjectiveResult(Map<String, ObjectiveGroup> groups) {
        this.groups = groups;
    }

    @Override
    public Object getResult() {
        return getGroups();
    }

    public Map<String, ObjectiveGroup> getGroups() {
        return groups;
    }
}
