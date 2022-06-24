package swcnoops.server.session.creature;

import swcnoops.server.datasource.CreatureSettings;

public class CreatureManagerImpl implements CreatureManager {
    final private CreatureDataMap creatureDataMap;
    final private CreatureSettings creatureSettings;

    protected CreatureManagerImpl(CreatureDataMap creatureDataMap, CreatureSettings creatureSettings) {
        this.creatureDataMap = creatureDataMap;
        this.creatureSettings = creatureSettings;
    }

    @Override
    public void recaptureCreature(String creatureTroopUid, long time) {
        if (hasCreature()) {
            this.creatureSettings.recapture(time + creatureDataMap.trapData.getRearmTime());
        }
    }

    @Override
    public boolean hasCreature() {
        return this.getCreatureStatus() != CreatureStatus.Invalid;
    }

    @Override
    public String getBuildingKey() {
        return this.creatureDataMap.building.key;
    }

    @Override
    public String getBuildingUid() {
        return this.creatureDataMap.building.uid;
    }

    @Override
    public long getRecaptureEndTime() {
        return this.creatureSettings.getRecaptureEndTime();
    }

    @Override
    public boolean isCreatureAlive() {
        if (this.getCreatureStatus() != CreatureStatus.Alive) {
            if (this.creatureSettings.hasBeenRecaptured()) {
                this.creatureBuyout();
            }
        }

        return this.getCreatureStatus() == CreatureStatus.Alive;
    }

    @Override
    public boolean isRecapturing() {
        return this.getCreatureStatus() == CreatureStatus.Recapturing;
    }

    @Override
    public String getCreatureUid() {
        return this.getCreatureSettings().getCreatureUid();
    }

    @Override
    public String getSpecialAttackUid() {
        return this.creatureDataMap.trapData.getEventData();
    }

    @Override
    public void creatureBuyout() {
        this.creatureSettings.setCreatureStatus(CreatureStatus.Alive);
    }

    @Override
    public CreatureStatus getCreatureStatus() {
        return this.creatureSettings.getCreatureStatus();
    }

    @Override
    public CreatureSettings getCreatureSettings() {
        return this.creatureSettings;
    }
}
