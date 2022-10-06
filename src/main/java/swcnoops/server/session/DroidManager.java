package swcnoops.server.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.game.*;
import swcnoops.server.model.CurrencyType;
import swcnoops.server.session.map.MapItem;
import swcnoops.server.session.training.BuildUnit;
import swcnoops.server.session.training.TrainingManagerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

public class DroidManager implements Constructor {
    private static final Logger LOG = LoggerFactory.getLogger(DroidManager.class);

    static final private TrainingManagerFactory trainingManagerFactory = new TrainingManagerFactory();

    private Collection<BuildUnit> unitsInQueue = new LinkedList<>();
    final private PlayerSession playerSession;

    public DroidManager(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    public Collection<BuildUnit> getUnitsInQueue() {
        return unitsInQueue;
    }

    @Override
    public void removeCompletedBuildUnit(BuildUnit buildUnit) {
        unitsInQueue.remove(buildUnit);
    }

    public void addBuildUnit(BuildUnit buildUnit) {
        unitsInQueue.add(buildUnit);
    }

    public void processCompletedBuildUnits(long time) {
        Iterator<BuildUnit> buildUnitsIterator = this.unitsInQueue.iterator();
        while(buildUnitsIterator.hasNext()) {
            BuildUnit buildUnit = buildUnitsIterator.next();
            // units are sorted by endTime
            if (buildUnit.getEndTime() <= time) {
                buildUnitsIterator.remove();
                MapItem mapItem = this.playerSession.getMapItemByKey(buildUnit.getBuildingId());
                if (buildUnit.getContractType() == ContractType.Upgrade) {
                    mapItem.upgradeComplete(this.playerSession, buildUnit.getUnitId(), buildUnit.getTag(), buildUnit.getEndTime());
                } else if (buildUnit.getContractType() == ContractType.Build) {
                    mapItem.buildComplete(this.playerSession, buildUnit.getUnitId(), buildUnit.getTag(), buildUnit.getEndTime());
                    this.trainingManagerFactory.constructCompleteForBuilding(this.playerSession.getTrainingManager(), mapItem);
                } else if (buildUnit.getContractType() == ContractType.Clear) {
                    this.playerSession.removeMapItemByKey(mapItem.getBuildingKey());
                    int expectedDelta = mapItem.getBuilding().currentStorage;
                    CurrencyDelta currencyDelta = new CurrencyDelta(expectedDelta, expectedDelta, CurrencyType.crystals, false);
                    this.playerSession.processInventoryStorage(currencyDelta);
                }
            }
        }
    }

    public CurrencyDelta buyout(String buildingId, int crystals, long time) {
        BuildUnit buildUnit = getBuildUnitById(buildingId);
        CurrencyDelta currencyDelta = null;
        if (buildUnit != null) {
            int secondsBuyingOut = (int)(buildUnit.getEndTime() - time);
            buildUnit.setEndTime(time);
            processCompletedBuildUnits(time);
            BuildingData buildingData = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(buildUnit.getUnitId());
            int crystalsToBuy = CrystalHelper.secondsToCrystals(secondsBuyingOut, buildingData);
            int givenDelta = CrystalHelper.calculateGivenCrystalDeltaToRemove(this.playerSession, crystals);
            currencyDelta = new CurrencyDelta(givenDelta, crystalsToBuy, CurrencyType.crystals, true);
        }

        return currencyDelta;
    }

    private BuildUnit getBuildUnitById(String buildingId) {
        Optional<BuildUnit> found = this.unitsInQueue.stream()
                .filter(a -> a.getBuildingId().equals(buildingId)).findFirst();

        BuildUnit buildUnit = null;
        if (found.isPresent())
            buildUnit = found.get();

        return buildUnit;
    }

    public void constructBuildUnit(MapItem mapItem, String tag, long time, int expectedCost) {
        BuildUnit buildUnit = new BuildUnit(this, mapItem.getBuildingKey(),
                mapItem.getBuildingData().getUid(), expectedCost, ContractType.Build, tag);
        buildUnit.setStartTime(time);
        buildUnit.setEndTime(time + mapItem.getBuildingData().getTime());
        mapItem.getBuilding().lastCollectTime = buildUnit.getEndTime();
        mapItem.setupForConstruction();
        this.addBuildUnit(buildUnit);
    }

    public CurrencyDelta clearMapItem(MapItem mapItem, int credits, int materials, int contraband, long time) {
        CurrencyType currencyType = CurrencyHelper.getCurrencyType(mapItem.getBuildingData());
        int expectedCost = CurrencyHelper.getConstructionCost(mapItem, currencyType);

        BuildUnit buildUnit = new BuildUnit(this, mapItem.getBuildingKey(),
                mapItem.getBuildingData().getUid(), expectedCost, ContractType.Clear, null);
        buildUnit.setStartTime(time);
        buildUnit.setEndTime(time + mapItem.getBuildingData().getTime());
        this.addBuildUnit(buildUnit);

        int givenTotal = CurrencyHelper.getGivenTotal(currencyType, credits, materials, contraband);
        int givenCost = CurrencyHelper.calculateGivenConstructionCost(this.playerSession, givenTotal, currencyType);
        CurrencyDelta currencyDelta = new CurrencyDelta(givenCost, expectedCost, currencyType, true);
        return currencyDelta;
    }

    public CurrencyDelta upgradeBuildUnit(MapItem mapItem, String tag, int credits, int materials, int contraband, long time) {
        CurrencyType currencyType = CurrencyHelper.getCurrencyType(mapItem);
        if (currencyType == null)
            throw new RuntimeException("Can not determine currency type to build");

        BuildingData nextLevelBuildingData = ServiceFactory.instance().getGameDataManager()
                .getBuildingDataByBuildingId(mapItem.getBuildingData().getBuildingID(),
                        mapItem.getBuildingData().getLevel() + 1);

        if (nextLevelBuildingData == null) {
            LOG.error("Failed to get next building level for player " + this.playerSession.getPlayerId() + " for mapItem " +
                    mapItem.getBuildingData().getBuildingID());
            return null;
        }

        int expectedCost = CurrencyHelper.getConstructionCost(nextLevelBuildingData, currencyType);

        BuildUnit buildUnit = new BuildUnit(this, mapItem.getBuildingKey(),
                nextLevelBuildingData.getUid(), expectedCost, ContractType.Upgrade, tag);
        buildUnit.setStartTime(time);
        buildUnit.setEndTime(time + nextLevelBuildingData.getTime());
        this.addBuildUnit(buildUnit);

        int givenTotal = CurrencyHelper.getGivenTotal(currencyType, credits, materials, contraband);
        int givenDelta = CurrencyHelper.calculateGivenConstructionCost(this.playerSession, givenTotal, currencyType);
        CurrencyDelta currencyDelta = new CurrencyDelta(givenDelta, expectedCost, currencyType, true);
        return currencyDelta;
    }

    public CurrencyDelta cancel(String buildingId, int credits, int materials, int contraband, long time) {
        BuildUnit buildUnit = getBuildUnitById(buildingId);
        CurrencyDelta currencyDelta = null;
        if (buildUnit != null) {
            this.unitsInQueue.remove(buildUnit);

            BuildingData buildingData = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(buildUnit.getUnitId());
            CurrencyType buildCurrency = CurrencyHelper.getCurrencyType(buildingData);
            int givenDelta = CurrencyHelper.calculateGivenRefund(this.playerSession, credits, materials, contraband, buildCurrency);
            GameConstants constants = ServiceFactory.instance().getGameDataManager().getGameConstants();
            int expectedRefund = (int)((float) buildUnit.getCost() * constants.contract_refund_percentage_buildings / 100f);

            int availableStorage = CurrencyHelper.calculateStorageAvailable(buildCurrency, playerSession);
            if (expectedRefund > availableStorage)
                expectedRefund = availableStorage;
            currencyDelta = new CurrencyDelta(givenDelta, expectedRefund, buildCurrency, false);
            MapItem mapItem = this.playerSession.getPlayerMapItems().getMapItemByKey(buildingId);
            if (mapItem != null) {
                mapItem.upgradeCancelled(time);
            }
        }

        return currencyDelta;
    }

    public CurrencyDelta buildingSwap(MapItem mapItem, String buildingUid, int credits, int materials, int contraband, long time) {

        CurrencyType currencyType = CurrencyHelper.getCurrencyType(mapItem.getBuildingData());
        int crossCost = CurrencyHelper.getCrossCost(mapItem.getBuildingData(), currencyType);

        BuildUnit buildUnit = new BuildUnit(this, mapItem.getBuildingKey(),
                buildingUid, crossCost, ContractType.Upgrade, null);
        buildUnit.setStartTime(time);
        buildUnit.setEndTime(time + mapItem.getBuildingData().getCrossTime());
        this.addBuildUnit(buildUnit);

        int givenTotal = CurrencyHelper.getGivenTotal(currencyType, credits, materials, contraband);
        int givenDelta = CurrencyHelper.calculateGivenConstructionCost(this.playerSession, givenTotal, currencyType);
        return new CurrencyDelta(givenDelta, crossCost, currencyType, true);
    }
}
