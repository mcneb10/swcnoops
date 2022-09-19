package swcnoops.server.datasource;

import org.mongojack.Id;
import swcnoops.server.model.FactionType;

import java.util.Date;
import java.util.List;

public class WarSignUp {
    @Id
    public String _id;
    public FactionType faction;
    public String guildId;
    public List<String> participantIds;
    public long time;
    public Date signUpdate;
    public boolean isSameFactionWarAllowed;
    public String guildName;
    public String icon;
}
