package swcnoops.server.model;

import java.util.Map;

public class TroopDonationData extends SquadNotificationData {
    public Map<String,Integer> troopsDonated;
    public int amount;
    public String requestId;
    public String recipientId;

    public TroopDonationData() {
        super("TroopDonation");
    }
}
