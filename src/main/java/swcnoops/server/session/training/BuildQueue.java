package swcnoops.server.session.training;

import swcnoops.server.game.TroopData;
import swcnoops.server.session.PlayerSession;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A queue to represent the queues in a builder (barrack, factory etc...)
 * The way these buildings work is to group the same units to build as a group.
 * This class models that behavior.
 */
public class BuildQueue {
    final private LinkedList<BuildSlot> buildQueue = new LinkedList<>();
    final private Map<String, BuildSlot> buildQueueMap = new HashMap<>();

    protected LinkedList<BuildSlot> getBuildQueue() {
        return buildQueue;
    }

    protected boolean isEmpty() {
        return this.buildQueue.size() == 0;
    }

    final private PlayerSession playerSession;
    public BuildQueue(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    protected void add(List<BuildUnit> buildUnits) {
        BuildUnit buildUnit = buildUnits.get(0);
        BuildSlot buildSlot = this.getOrCreateBuildSlot(buildUnit);
        buildSlot.addBuildUnits(buildUnits);
    }

    private BuildSlot getOrCreateBuildSlot(BuildUnit buildUnit) {
        BuildSlot buildSlot = this.buildQueueMap.get(buildUnit.getUnitId());
        if (buildSlot == null) {
            buildSlot = createBuildSlot(buildUnit);
            this.buildQueueMap.put(buildSlot.getUnitId(), buildSlot);
            this.buildQueue.add(buildSlot);
        }
        return buildSlot;
    }

    protected void add(BuildUnit buildUnit) {
        BuildSlot buildSlot = this.getOrCreateBuildSlot(buildUnit);
        buildSlot.addBuildUnit(buildUnit);
    }

    private BuildSlot createBuildSlot(BuildUnit buildUnit) {
        TroopData troopData = getPlayersTroop(buildUnit.getUnitId());
        return new BuildSlot(buildUnit.getUnitId(), troopData);
    }

    private TroopData getPlayersTroop(String unitId) {
        TroopData troopData = this.playerSession.getTroopInventory().getTroopByUnitId(unitId);

        if (troopData == null)
            throw new RuntimeException("Failed to get TroopData for " + unitId);

        return troopData;
    }

    protected void recalculateEndTimes(long startTime) {
        for (BuildSlot buildSlot : this.buildQueue) {
            startTime = buildSlot.recalculateEndTimes(startTime);
        }
    }

    protected void removeBuildSlotIfEmpty(BuildSlot buildSlot) {
        if (buildSlot.isEmpty()) {
            this.buildQueue.remove(buildSlot);
            this.buildQueueMap.remove(buildSlot.getUnitId());
        }
    }

    protected List<BuildUnit> remove(String unitTypeId, int quantity, boolean fromBack) {
        List<BuildUnit> removed = null;
        BuildSlot buildSlot = this.buildQueueMap.get(unitTypeId);
        if (buildSlot != null) {
            removed = buildSlot.remove(quantity, fromBack);
            removeBuildSlotIfEmpty(buildSlot);
        }
        return removed;
    }
}
