package swcnoops.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectiveProgress {
    public String uid;
    public String planetId;
    public int hq = 1;
    public int count;
    public int target = 1;
    public ObjectiveState state;
    public boolean claimAttempt;
    public Long receivedStartCount;
}
