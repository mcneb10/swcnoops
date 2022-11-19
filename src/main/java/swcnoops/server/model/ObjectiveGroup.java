package swcnoops.server.model;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveGroup {
    public long endTime;
    public long graceTime;
    public String groupId;
    public List<ObjectiveProgress> progress = new ArrayList<>();
    public long startTime;
    public String planetId;

    public ObjectiveGroup() {
    }

    public ObjectiveGroup(String groupId, String planetId) {
        this.groupId = groupId;
        this.planetId = planetId;
    }

    public String getPlanetId() {
        return planetId;
    }
}
