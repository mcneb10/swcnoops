package swcnoops.server.session.creature;

import swcnoops.server.datasource.Creature;

public class CreatureManagerImpl implements CreatureManager {
    final private CreatureDataMap creatureDataMap;
    final private Creature creature;

    protected CreatureManagerImpl(CreatureDataMap creatureDataMap, Creature creature) {
        this.creatureDataMap = creatureDataMap;
        this.creature = creature;
    }

    @Override
    public void recaptureCreature(String creatureTroopUid, long time) {
        if (hasCreature()) {
            this.creature.recapture(time + creatureDataMap.trapData.getRearmTime());
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
        return this.creature.getRecaptureEndTime();
    }

    @Override
    public boolean isCreatureAlive() {
        if (this.getCreatureStatus() != CreatureStatus.Alive) {
            if (this.creature.hasBeenRecaptured()) {
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
        return this.getCreature().getCreatureUid();
    }

    @Override
    public String getSpecialAttackUid() {
        return this.creatureDataMap.trapData.getEventData();
    }

    @Override
    public void creatureBuyout() {
        this.creature.setCreatureStatus(CreatureStatus.Alive);
    }

    @Override
    public CreatureStatus getCreatureStatus() {
        return this.creature.getCreatureStatus();
    }

    @Override
    public Creature getCreature() {
        return this.creature;
    }
}
