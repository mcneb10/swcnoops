package swcnoops.server.commands.player.response;

import swcnoops.server.requests.AbstractCommandResult;
import swcnoops.server.model.Liveness;
import swcnoops.server.model.PlayerModel;

import java.util.Map;

public class PlayerLoginCommandResult extends AbstractCommandResult {
    public long created;
    public boolean firstTimePlayer;
    public Long lastTroopRequestTime;
    public Long lastWarTroopRequestTime;
    public String locale;
    public String name;
    public String network;
    public String viewNetwork;
    public String playerId;

    public Object abTests;
    public Map<String, String> sharedPrefs;
    public boolean pushRewarded;
    public PlayerModel playerModel;
    public Object triggers;
    public Liveness liveness;
    public Object clientPrefs;
    public Object purchaseTracking;
    public Object scalars;
}

