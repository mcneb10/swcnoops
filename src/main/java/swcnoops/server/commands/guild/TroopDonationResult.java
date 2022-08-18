package swcnoops.server.commands.guild;

import swcnoops.server.model.SquadNotification;

import java.util.Map;

public class TroopDonationResult {
    private SquadNotification squadNotification;
    private Map<String, Integer> donatedTroops;

    public TroopDonationResult(SquadNotification squadNotification, Map<String, Integer> donatedTroops) {
        this.squadNotification = squadNotification;
        this.donatedTroops = donatedTroops;
    }

    public SquadNotification getSquadNotification() {
        return squadNotification;
    }

    public Map<String, Integer> getDonatedTroops() {
        return donatedTroops;
    }
}
