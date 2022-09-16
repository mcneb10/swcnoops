package swcnoops.server.model;

import java.io.Serializable;

public class Building implements Serializable {
    public String key;
    public int x;
    public int z;
    public String uid;
    public long lastCollectTime;
    public int currentStorage;
}
