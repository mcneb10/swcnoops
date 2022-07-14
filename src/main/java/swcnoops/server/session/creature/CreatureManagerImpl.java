package swcnoops.server.session.creature;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import swcnoops.server.datasource.Creature;
import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.model.Position;

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
    public BuildingData getBuildingData() {
        return this.creatureDataMap.buildingData;
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
                this.buyout(0);
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
    public void buyout(long time) {
        this.creature.setCreatureStatus(CreatureStatus.Alive);
    }

    @Override
    public void cancel(long time) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CreatureStatus getCreatureStatus() {
        return this.creature.getCreatureStatus();
    }

    @Override
    public Creature getCreature() {
        return this.creature;
    }

    @Override
    public void collect(long time) {
        throw new NotImplementedException();
    }

    @Override
    public void moveTo(Position newPosition) {
        throw new NotImplementedException();
    }

    @Override
    public Building getBuilding() {
        throw new NotImplementedException();
    }

    @Override
    public void changeBuildingData(BuildingData buildingData) {
        throw new NotImplementedException();
    }

    @Override
    public void upgradeComplete(String unitId) {
        throw new NotImplementedException();
    }
}
