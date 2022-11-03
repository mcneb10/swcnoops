package swcnoops.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Raid {
    public long startTime;
    public long nextRaidStartTime;
    public long endTime;
    public String planetId;
    public String raidPoolId;
    public String raidId;
    public String raidMissionId;
    @JsonIgnore
    public long raidStartTimeNoOffset;
}
