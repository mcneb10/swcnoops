package swcnoops.server.model;

public class TroopDonationProgress {
    private int donationCount;
    private long lastTrackedDonationTime;
    private long repDonationCooldownEndTime;

    public TroopDonationProgress(int donationCount, long lastTrackedDonationTime, long repDonationCooldownEndTime) {
        this.donationCount = donationCount;
        this.lastTrackedDonationTime = lastTrackedDonationTime;
        this.repDonationCooldownEndTime = repDonationCooldownEndTime;
    }

    public int getDonationCount() {
        return donationCount;
    }

    public long getLastTrackedDonationTime() {
        return lastTrackedDonationTime;
    }

    public long getRepDonationCooldownEndTime() {
        return repDonationCooldownEndTime;
    }
}
