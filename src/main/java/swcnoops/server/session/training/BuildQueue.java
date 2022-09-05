package swcnoops.server.session.training;

import swcnoops.server.session.PlayerSession;

import java.util.*;

/**
 * A queue to represent the queues in a builder (barrack, factory etc...)
 * The way these buildings work is to group the same units to build as a group.
 * This class models that behavior.
 */
public class BuildQueue {
    final private LinkedList<BuildSlot> buildQueue = new LinkedList<>();
    final private Map<String, BuildSlot> buildQueueMap = new HashMap<>();
    final private PlayerSession playerSession;

    protected LinkedList<BuildSlot> getBuildQueue() {
        return buildQueue;
    }

    protected boolean isEmpty() {
        return this.buildQueue.size() == 0;
    }

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
        // TODO - maybe register to the players session that they want to listen for upgrade events
        return new BuildSlot(buildUnit.getUnitId(), this);
    }

    protected void recalculateBuildUnitTimes(long startTime) {
        for (BuildSlot buildSlot : this.buildQueue) {
            startTime = buildSlot.recalculateBuildUnitTimes(startTime);
        }
    }

    protected void removeBuildSlotIfEmpty(BuildSlot buildSlot) {
        if (buildSlot.isEmpty()) {
            // TODO - maybe unregister to the players session that they want to listen for upgrade events
            this.buildQueue.remove(buildSlot);
            this.buildQueueMap.remove(buildSlot.getUnitId());
        }
    }

    protected List<BuildUnit> remove(String unitTypeId, int quantity, boolean fromBack) {
        List<BuildUnit> removed = new ArrayList<>();
        BuildSlot buildSlot = this.buildQueueMap.get(unitTypeId);
        if (buildSlot != null) {
            removed = buildSlot.remove(quantity, fromBack);
            removeBuildSlotIfEmpty(buildSlot);
        }
        return removed;
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public List<BuildUnit> removeAll(String unitTypeId) {
        List<BuildUnit> removed = new ArrayList<>();
        BuildSlot buildSlot = this.buildQueueMap.get(unitTypeId);
        if (buildSlot != null) {
            removed = buildSlot.remove(buildSlot.size(), true);
            removeBuildSlotIfEmpty(buildSlot);
        }
        return removed;
    }
}
