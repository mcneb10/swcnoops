package swcnoops.server.model;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveGroup {
    public long endTime;
    public long graceTime;
    public String groupId;
    public List<ObjectiveProgress> progress = new ArrayList<>();
    public long startTime;

    public ObjectiveGroup(String groupId) {
        this.groupId = groupId;
    }
}
