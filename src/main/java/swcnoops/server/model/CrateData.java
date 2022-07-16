package swcnoops.server.model;

import java.util.ArrayList;
import java.util.List;

public class CrateData {
    public boolean claimed;
    public String context;
    public String crateId;
    public boolean doesExpire;
    public long expires;
    public String guildId;
    public int hqLevel;
    public String planet;
    public long received;
    public List<SupplyData> resolvedSupplies = new ArrayList<>();
    public String uid;
    public String warId;
}
