package swcnoops.server.model;

public class TroopRequestData extends SquadNotificationData {
    /**
     * Players SC capacity
     */
    public int totalCapacity;
    /**
     * Players SC capacity available space
     */
    public int amount;
    /**
     * Some limiter to number of troops, set this to totalCapacity
     */
    public int troopDonationLimit;
    public String warId;

    public TroopRequestData() {
        super("TroopRequest");
    }
}
