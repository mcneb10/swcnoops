package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.session.creature.CreatureStatus;

public class CreatureSettings {
    private CreatureStatus creatureStatus;
    private String specialAttack;
    private long recaptureEndTime;
    private String creatureUid;

    public CreatureStatus getCreatureStatus() {
        return creatureStatus;
    }

    public String getSpecialAttack() {
        return specialAttack;
    }

    public long getRecaptureEndTime() {
        return recaptureEndTime;
    }

    public String getCreatureUid() {
        return creatureUid;
    }

    public void setCreatureStatus(CreatureStatus creatureStatus) {
        this.creatureStatus = creatureStatus;

        if (this.creatureStatus == CreatureStatus.Alive)
            this.recaptureEndTime = 0;
    }

    public void setSpecialAttack(String specialAttack) {
        this.specialAttack = specialAttack;
    }

    public void setRecaptureEndTime(long recaptureEndTime) {
        this.recaptureEndTime = recaptureEndTime;
    }

    public void setCreatureUid(String creatureUid) {
        this.creatureUid = creatureUid;
    }

    public void recapture(long endTime) {
        this.creatureStatus = CreatureStatus.Recapturing;
        this.recaptureEndTime = endTime;
    }

    public boolean hasBeenRecaptured() {
        if (this.creatureStatus == CreatureStatus.Recapturing) {
            if (this.getRecaptureEndTime() <= ServiceFactory.getSystemTimeSecondsFromEpoch())
                return true;
        }

        return false;
    }
}
