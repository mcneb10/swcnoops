package swcnoops.server.game;

import org.mongojack.Id;

public class Patch {
    @Id
    public String patchName;
    public int version;
    public String crcChecksum;
}
