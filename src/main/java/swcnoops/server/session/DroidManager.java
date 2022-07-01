package swcnoops.server.session;

import swcnoops.server.game.ContractType;
import swcnoops.server.session.map.MoveableMapItem;
import swcnoops.server.session.training.BuildUnit;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

public class DroidManager implements Constructor {
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
                if (buildUnit.getContractType() == ContractType.Upgrade) {
                    MoveableMapItem moveableMapItem = this.playerSession.getMapItemByKey(buildUnit.getBuildingId());
                    moveableMapItem.upgradeComplete(time);
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

    public void constructBuildUnit(MoveableMapItem moveableMapItem, long time) {
        BuildUnit buildUnit = new BuildUnit(this, moveableMapItem.getBuildingKey(),
                moveableMapItem.getBuildingData().getBuildingID(), ContractType.Build);
        buildUnit.setStartTime(time);
        buildUnit.setEndTime(time + moveableMapItem.getBuildingData().getTime());
        this.addBuildUnit(buildUnit);
    }

    public void upgradeBuildUnit(MoveableMapItem moveableMapItem, long time) {
        BuildUnit buildUnit = new BuildUnit(this, moveableMapItem.getBuildingKey(),
                moveableMapItem.getBuildingKey(), ContractType.Upgrade);
        buildUnit.setStartTime(time);
        buildUnit.setEndTime(time + moveableMapItem.getBuildingData().getTime());
        this.addBuildUnit(buildUnit);
    }
}
