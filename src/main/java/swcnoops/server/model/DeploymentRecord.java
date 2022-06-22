package swcnoops.server.model;

public class DeploymentRecord {
    private String uid;
    private String action;
    private long time;
    private int x;
    private int z;

    public String getUid() {
        return uid;
    }

    public String getAction() {
        return action;
    }

    public long getTime() {
        return time;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
