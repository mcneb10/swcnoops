package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.session.creature.CreatureStatus;

public class Creature {
    private CreatureStatus creatureStatus;
    private long recaptureEndTime;

    private String creatureUnitId;

    public CreatureStatus getCreatureStatus() {
        return creatureStatus;
    }

    public long getRecaptureEndTime() {
        return recaptureEndTime;
    }

    public String getCreatureUnitId() {
        return creatureUnitId;
    }

    public void setCreatureUnitId(String creatureUnitId) {
        this.creatureUnitId = creatureUnitId;
    }

    public void setCreatureStatus(CreatureStatus creatureStatus) {
        this.creatureStatus = creatureStatus;

        if (this.creatureStatus == CreatureStatus.Alive)
            this.recaptureEndTime = 0;
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
