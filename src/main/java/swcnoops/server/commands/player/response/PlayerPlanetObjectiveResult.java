package swcnoops.server.commands.player.response;

import swcnoops.server.model.ObjectiveGroup;
import swcnoops.server.requests.AbstractCommandResult;

import java.util.HashMap;
import java.util.Map;

// TODO - to finish to provide planet objectives
public class PlayerPlanetObjectiveResult extends AbstractCommandResult {
    private Map<String, ObjectiveGroup> groups = new HashMap<>();

    @Override
    public Object getResult() {
        return getGroups();
    }

    public Map<String, ObjectiveGroup> getGroups() {
        return groups;
    }
}
