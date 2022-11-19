package swcnoops.server.session.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TroopData;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.map.MapItem;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is to model the games queuing system for its troops, when they are built, completed and moved to being
 * a deployable unit ready for war.
 */
public class DeployableQueue {
    private static final Logger LOG = LoggerFactory.getLogger(DeployableQueue.class);
    private int totalDeployable;
    final private Map<String, Integer> deployableUnits = new HashMap<>();
    final private List<BuildUnit> unitsInQueue = new ArrayList<>();
    final private List<MapItem> mapItems = new ArrayList<>();
    final private PlayerSession playerSession;

    public DeployableQueue(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    public void addStorage(MapItem mapItem)
    {
        if (!mapItems.contains(mapItem))
            mapItems.add(mapItem);
    }

    private int getAvailableCapacity() {
        AtomicInteger totalStorage = new AtomicInteger();
        mapItems.stream().forEach(a -> totalStorage.addAndGet(a.getBuildingData().getStorage()));
        return totalStorage.get() - this.totalDeployable;
    }

    private void moveToDeployable(BuildUnit buildUnit) {
        TroopData troopData = buildUnit.getBuildSlot().getTroopData();
        this.totalDeployable += troopData.getSize();
        Integer numberOfUnits = this.deployableUnits.get(buildUnit.getUnitId());
        if (numberOfUnits == null)
            numberOfUnits = Integer.valueOf(0);

        numberOfUnits = Integer.valueOf(numberOfUnits.intValue() + 1);
        this.deployableUnits.put(buildUnit.getUnitId(), numberOfUnits);
    }

    public Map<String, Integer> getDeployableUnits() {
        return deployableUnits;
    }

    public int removeDeployable(String key, Integer value) {
        int removed = 0;

        if (this.deployableUnits.containsKey(key)) {

            int onBoard = this.deployableUnits.get(key).intValue();
            // number from client is negative
            int numberToRemove = Math.abs(value.intValue());
            if (onBoard < numberToRemove) {
                numberToRemove = onBoard;
                LOG.warn("Removing of deployable had to adjust the amount " + key + " from number " + value + " to "
                        + numberToRemove + " " + this.playerSession.getPlayerId());
            }

            onBoard = onBoard - numberToRemove;
            TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUnitId(key, 1);
            this.totalDeployable -= numberToRemove * troopData.getSize();
            if (onBoard == 0) {
                this.deployableUnits.remove(key);
            } else {
                this.deployableUnits.put(key, Integer.valueOf(onBoard));
            }

            removed = numberToRemove;
        }

        return removed;
    }

    protected List<BuildUnit> findAndMoveCompletedUnitsToDeployable(long clientTime) {
        List<BuildUnit> completed = new ArrayList<>();
        Iterator<BuildUnit> buildUnitsIterator = this.unitsInQueue.iterator();
        while(buildUnitsIterator.hasNext()) {
            BuildUnit buildUnit = buildUnitsIterator.next();
            // units are sorted by endTime
            if (buildUnit.getEndTime() > clientTime) {
                break;
            }

            // is there enough space to move this completed troop to the transport
            int availableCapacity = this.getAvailableCapacity();
            if (buildUnit.getBuildSlot().getTroopData().getSize() <= availableCapacity) {
                if (buildUnit.isHeadOfItsBuildQueue()) {
                    buildUnitsIterator.remove();
                    buildUnit.getConstructor().removeCompletedBuildUnit(buildUnit);
                    this.moveUnitToDeployable(buildUnit);
                    completed.add(buildUnit);
                }
            }
        }

        return completed;
    }

    private void moveUnitToDeployable(BuildUnit buildUnit) {
        this.unitsInQueue.remove(buildUnit);
        this.moveToDeployable(buildUnit);
    }

    protected void sortUnitsInQueue() {
        this.unitsInQueue.sort((a, b) -> a.compareEndTime(b));
    }

    public List<BuildUnit> getUnitsInQueue() {
        return this.unitsInQueue;
    }

    public void addUnitsToQueue(List<BuildUnit> buildUnits) {
        if (buildUnits != null)
            buildUnits.forEach(a -> addUnitsToQueue(a));
    }

    public void addUnitsToQueue(BuildUnit buildUnit) {
        this.unitsInQueue.add(buildUnit);
    }

    public void initialiseDeployableUnits(Map<String, Integer> storage) {
        this.deployableUnits.clear();
        this.deployableUnits.putAll(storage);

        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        this.totalDeployable = 0;
        for (Map.Entry<String, Integer> entry : this.deployableUnits.entrySet()) {
            TroopData troopData = gameDataManager.getTroopDataByUnitId(entry.getKey(), 1);
            this.totalDeployable += troopData.getSize() * entry.getValue();
        }
    }

    public void removeUnits(List<BuildUnit> removed, boolean isBuyout) {
        if (!isBuyout) {
            this.removeUnitsFromQueue(removed);
        } else {
            this.buyOutUnitToDeployable(removed);
        }
    }

    private void removeUnitsFromQueue(List<BuildUnit> buildUnits) {
        if (buildUnits != null)
            this.unitsInQueue.removeAll(buildUnits);
    }

    private void buyOutUnitToDeployable(List<BuildUnit> buildUnits) {
        if (buildUnits != null) {
            for (BuildUnit buildUnit : buildUnits) {
                moveUnitToDeployable(buildUnit);
            }
        }
    }

    public void removeUnits(BuildUnit buildUnit) {
        if (buildUnit != null)
            this.unitsInQueue.remove(buildUnit);
    }

    public List<BuildUnit> getNearestBuildUnits(String unitId, int remaining) {
        List<BuildUnit> nearest = new ArrayList<>(remaining);

        for (BuildUnit buildUnit : this.getUnitsInQueue()) {
            if (buildUnit != null && buildUnit.getUnitId() != null && buildUnit.getUnitId().equals(unitId)) {
                nearest.add(buildUnit);
                remaining--;
                if (remaining == 0)
                    break;
            }
        }

        return nearest;
    }
}
