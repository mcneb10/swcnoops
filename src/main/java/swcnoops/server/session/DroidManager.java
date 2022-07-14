package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.ContractType;
import swcnoops.server.session.map.MapItem;
import swcnoops.server.session.training.BuildUnit;
import swcnoops.server.session.training.TrainingManagerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

public class DroidManager implements Constructor {
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

    public void moveCompletedBuildUnits(long time) {
        Iterator<BuildUnit> buildUnitsIterator = this.unitsInQueue.iterator();
        while(buildUnitsIterator.hasNext()) {
            BuildUnit buildUnit = buildUnitsIterator.next();
            // units are sorted by endTime
            if (buildUnit.getEndTime() <= time) {
                buildUnitsIterator.remove();
                MapItem mapItem = this.playerSession.getMapItemByKey(buildUnit.getBuildingId());
                if (buildUnit.getContractType() == ContractType.Upgrade) {
                    mapItem.upgradeComplete(buildUnit.getUnitId());
                } else if (buildUnit.getContractType() == ContractType.Build) {
                    this.trainingManagerFactory.constructCompleteForBuilding(this.playerSession.getTrainingManager(), mapItem);
                }
            }
        }
    }

    public void buyout(String buildingId, long time) {
        BuildUnit buildUnit = getBuildUnitById(buildingId);
        if (buildUnit != null) {
            buildUnit.setEndTime(time);
            moveCompletedBuildUnits(time);
        }
    }

    private BuildUnit getBuildUnitById(String buildingId) {
        Optional<BuildUnit> found = this.unitsInQueue.stream()
                .filter(a -> a.getBuildingId().equals(buildingId)).findFirst();

        BuildUnit buildUnit = null;
        if (found.isPresent())
            buildUnit = found.get();

        return buildUnit;
    }

    public void constructBuildUnit(MapItem moveableMapItem, long time) {
        BuildUnit buildUnit = new BuildUnit(this, moveableMapItem.getBuildingKey(),
                moveableMapItem.getBuildingData().getUid(), ContractType.Build);
        buildUnit.setStartTime(time);
        buildUnit.setEndTime(time + moveableMapItem.getBuildingData().getTime());
        this.addBuildUnit(buildUnit);
    }

    public void upgradeBuildUnit(MapItem moveableMapItem, long time) {
        BuildingData nextLevelBuildingData = ServiceFactory.instance().getGameDataManager()
                .getBuildingDataByBuildingId(moveableMapItem.getBuildingData().getBuildingID(),
                        moveableMapItem.getBuildingData().getLevel() + 1);
        BuildUnit buildUnit = new BuildUnit(this, moveableMapItem.getBuildingKey(),
                nextLevelBuildingData.getUid(), ContractType.Upgrade);
        buildUnit.setStartTime(time);
        buildUnit.setEndTime(time + nextLevelBuildingData.getTime());
        this.addBuildUnit(buildUnit);
    }

    public void cancel(String buildingId) {
        BuildUnit buildUnit = getBuildUnitById(buildingId);
        if (buildUnit != null)
            this.unitsInQueue.remove(buildUnit);
    }

    public void buildingSwap(MapItem moveableMapItem, String buildingUid, long time) {
        BuildUnit buildUnit = new BuildUnit(this, moveableMapItem.getBuildingKey(),
                buildingUid, ContractType.Upgrade);
        buildUnit.setStartTime(time);
        buildUnit.setEndTime(time + moveableMapItem.getBuildingData().getCrossTime());
        this.addBuildUnit(buildUnit);
    }
}
