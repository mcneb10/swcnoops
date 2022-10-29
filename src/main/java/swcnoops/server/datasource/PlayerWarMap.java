package swcnoops.server.datasource;

import org.mongojack.Id;
import swcnoops.server.model.PlayerMap;

import java.util.Date;

public class PlayerWarMap extends PlayerMap {
    @Id
    public String playerId;
    public Date timestamp;
}
