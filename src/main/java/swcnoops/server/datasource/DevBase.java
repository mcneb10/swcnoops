package swcnoops.server.datasource;

import org.mongojack.Id;
import swcnoops.server.model.Buildings;

public class DevBase {
    @Id
    public String _id;
    public Buildings buildings;
    public int hq;
    public int xp;
    public String fileName;
    public long checksum;
}
