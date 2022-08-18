package swcnoops.server.model;

public class TroopRequestData implements SquadNotificationData {
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
}
