package swcnoops.server.model;

import java.util.ArrayList;
import java.util.List;

public class Contract {
    public String buildingId;

    public String contractType;

    public long endTime;

    List<String> perkIds = new ArrayList<>();

    public String uid;
}
