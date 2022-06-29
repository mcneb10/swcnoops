package swcnoops.server.model;

import java.util.Map;

public class TroopDonationData implements SquadNotificationData {
    public Map<String,Integer> troopsDonated;
    public int amount;
    public String requestId;
    public String recipientId;
}
