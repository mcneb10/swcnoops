package swcnoops.server.session.map;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.model.CurrencyType;
import swcnoops.server.model.Position;
import swcnoops.server.session.CurrencyDelta;
import swcnoops.server.session.PlayerSession;

abstract public class AbstractMapItem implements MapItem {
    private Building building;
    private BuildingData buildingData;

    public AbstractMapItem(Building building, BuildingData buildingData) {
        this.building = building;
        this.buildingData = buildingData;
    }

    @Override
    public Building getBuilding() {
        return building;
    }

    @Override
    public String getBuildingKey() {
        return building.key;
    }

    @Override
    public String getBuildingUid() {
        return building.uid;
    }

    @Override
    public BuildingData getBuildingData() {
        return this.buildingData;
    }

    public void moveTo(Position newPosition) {
        this.building.x = newPosition.x;
        this.building.z = newPosition.z;
    }

    @Override
    public CurrencyDelta collect(PlayerSession playerSession, int credits, int materials, int contraband, int crystals, long time) {
        int givenTotal = getGivenTotal(this.getBuildingData().getCurrency(), credits, materials, contraband);
        int givenDelta = calculateGivenDeltaCollected(this.getBuildingData().getCurrency(), givenTotal, playerSession);
        int expectedDelta = calculateExpectedDeltaCollect(this.building, this.buildingData, time);
        this.building.currentStorage = 0;
        this.building.lastCollectTime = time;
        return new CurrencyDelta(givenDelta, expectedDelta, this.getBuildingData().getCurrency());
    }

    private int calculateExpectedDeltaCollect(Building building, BuildingData buildingData, long time) {
        float timeDelta = time - building.lastCollectTime;
        int delta = (int)(timeDelta * (buildingData.getProduce()/buildingData.getCycleTime()));
        delta += building.currentStorage;
        if (delta > buildingData.getStorage())
            delta = buildingData.getStorage();
        return delta;
    }

    private int calculateGivenDeltaCollected(CurrencyType currency, int givenTotal, PlayerSession playerSession) {
        int givenDelta = givenTotal;
        if (currency != null) {
            switch (currency) {
                case credits:
                    givenDelta -= playerSession.getPlayerSettings().getInventoryStorage().credits.amount;
                    break;
                case materials:
                    givenDelta -= playerSession.getPlayerSettings().getInventoryStorage().materials.amount;
                    break;
                case contraband:
                    givenDelta -= playerSession.getPlayerSettings().getInventoryStorage().contraband.amount;
                    break;
            }
        }
        if (givenDelta < 0)
            givenDelta = 0;

        return givenDelta;
    }

    private int getGivenTotal(CurrencyType currency, int credits, int materials, int contraband) {
        int givenTotal = 0;
        if (currency != null) {
            switch (currency) {
                case credits:
                    givenTotal = credits;
                    break;
                case materials:
                    givenTotal = materials;
                    break;
                case contraband:
                    givenTotal = contraband;
                    break;
            }
        }
        return givenTotal;
    }

    public void upgradeComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        BuildingData upgradeBuildingData = ServiceFactory.instance().getGameDataManager()
                .getBuildingDataByUid(unitId);
        changeBuildingData(upgradeBuildingData);
    }

    @Override
    public void buildComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {

    }

    @Override
    public void changeBuildingData(BuildingData buildingData) {
        this.getBuilding().uid = buildingData.getUid();
        this.buildingData = buildingData;
    }
}
