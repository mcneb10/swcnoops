package swcnoops.server.session.creature;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Creature;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TrapData;
import swcnoops.server.game.TroopData;
import swcnoops.server.model.Building;
import swcnoops.server.model.Position;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.map.StrixBeacon;

public class CreatureManagerImpl implements CreatureManager {
    private CreatureDataMap creatureDataMap;
    private Creature creature;

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
    public void upgradeComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        throw new NotImplementedException();
    }

    @Override
    public void buildComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        throw new NotImplementedException();
    }

    @Override
    public void creatureTrapComplete(StrixBeacon strixBeacon, long endTime) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        TrapData trapData = gameDataManager.getTrapDataByUid(strixBeacon.getBuildingData().getTrapId());
        this.creatureDataMap = new CreatureDataMap(strixBeacon.getBuilding(), strixBeacon.getBuildingData(), trapData);

        // default is the rancour
        String unitId = strixBeacon.getBuildingData().getFaction().getNameForLookup() + "RancorCreature";
        TroopData creatureTroopData = gameDataManager.getLowestLevelTroopDataByUnitId(unitId);
        this.creature.setCreatureUid(creatureTroopData.getUid());
        this.buyout(endTime);
    }
}
