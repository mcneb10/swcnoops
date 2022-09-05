package swcnoops.server.session.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TroopData;
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

    public DeployableQueue() {
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

    public void removeDeployable(Map<String, Integer> deployables) {
        deployables.entrySet().iterator();

        Iterator<Map.Entry<String,Integer>> troopIterator = deployables.entrySet().iterator();
        while(troopIterator.hasNext()) {
            Map.Entry<String,Integer> entry = troopIterator.next();
            removeDeployable(entry.getKey(), entry.getValue());
        }
    }

    public void removeDeployable(String key, Integer value) {
        boolean removed = false;

        if (this.deployableUnits.containsKey(key)) {
            removed = true;

            int onBoard = this.deployableUnits.get(key).intValue();
            // number from client is negative
            int numberToRemove = Math.abs(value.intValue());
            if (onBoard < numberToRemove) {
                numberToRemove = onBoard;
                LOG.warn("Removing of deployable had to adjust the amount " + key + " from number " + value + " to " + numberToRemove);
            }

            onBoard = onBoard - numberToRemove;
            TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUnitId(key, 1);
            this.totalDeployable -= numberToRemove * troopData.getSize();
            if (onBoard == 0) {
                this.deployableUnits.remove(key);
            } else {
                this.deployableUnits.put(key, Integer.valueOf(onBoard));
            }
        }

        if (!removed) {
            LOG.warn("Removing of deployable did not remove " + key + " number " + value);
        }
    }

    public void findAndMoveCompletedUnitsToDeployable(long clientTime) {
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
                }
            }
        }
    }

    public void moveUnitToDeployable(List<BuildUnit> buildUnits) {
        if (buildUnits != null) {
            for (BuildUnit buildUnit : buildUnits) {
                moveUnitToDeployable(buildUnit);
            }
        }
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

    public void removeUnitsFromQueue(List<BuildUnit> buildUnits) {
        if (buildUnits != null)
            this.unitsInQueue.removeAll(buildUnits);
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
}
