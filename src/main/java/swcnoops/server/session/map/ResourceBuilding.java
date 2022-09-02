package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;
import swcnoops.server.game.CurrencyHelper;
import swcnoops.server.model.Building;
import swcnoops.server.model.CurrencyType;
import swcnoops.server.session.CurrencyDelta;
import swcnoops.server.session.PlayerSession;

public class ResourceBuilding extends MapItemImpl {
    public ResourceBuilding(Building building, BuildingData buildingData) {
        super(building, buildingData);
    }

    @Override
    public CurrencyDelta collect(PlayerSession playerSession, int credits, int materials, int contraband, int crystals,
                                 long time, boolean collectAll)
    {
        int givenTotal = getGivenTotal(this.getBuildingData().getCurrency(), credits, materials, contraband);
        int givenDelta = calculateGivenDeltaCollected(this.getBuildingData().getCurrency(), givenTotal, playerSession);
        int estimatedStorageAmount = estimateStorageAmount(this.building, this.buildingData, time);
        int storageAvailable = CurrencyHelper.calculateStorageAvailable(this.getBuildingData().getCurrency(), playerSession);

        int expectedDelta = estimatedStorageAmount;
        if (storageAvailable < expectedDelta)
            expectedDelta = storageAvailable;

        if (collectAll)
            givenDelta = expectedDelta;

        this.building.currentStorage = estimatedStorageAmount - givenDelta;

        if (this.building.currentStorage < 0) {
            this.building.currentStorage = 0;
        }

        this.building.lastCollectTime = time;
        return new CurrencyDelta(givenDelta, expectedDelta, this.getBuildingData().getCurrency(), false);
    }

    private int estimateStorageAmount(Building building, BuildingData buildingData, long time) {
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

    @Override
    public void upgradeComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        super.upgradeComplete(playerSession, unitId, tag, endTime);
        this.building.lastCollectTime = endTime;
    }

    @Override
    public void buildComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        this.building.lastCollectTime = endTime;
    }

    @Override
    public void upgradeCancelled(long time) {
        this.building.lastCollectTime = time;
    }
}
