package swcnoops.server.session.training;

import swcnoops.server.game.TroopData;
import swcnoops.server.session.PlayerSession;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This models a group of units of the same type to be built.
 */
public class BuildSlot {
    final private String unitId;
    final private PlayerSession playerSession;
    final private LinkedList<BuildUnit> buildUnits = new LinkedList<>();

    public BuildSlot(String unitId, PlayerSession playerSession) {
        this.unitId = unitId;
        this.playerSession = playerSession;
    }

    protected String getUnitId() {
        return unitId;
    }

    protected void addBuildUnits(List<BuildUnit> buildUnits) {
        for (BuildUnit buildUnit : buildUnits) {
            addBuildUnit(buildUnit);
        }
    }

    protected void addBuildUnit(BuildUnit buildUnit) {
        buildUnit.setBuildSlot(this);
        this.buildUnits.add(buildUnit);
    }

    protected List<BuildUnit> remove(int quantity, boolean fromBack) {
        List<BuildUnit> unitsRemoved = new ArrayList<>(quantity);
        if (quantity > this.buildUnits.size()) {
            quantity = this.buildUnits.size();
        }

        for (int i = 0; i < quantity; i++) {
            if (fromBack)
                unitsRemoved.add(this.buildUnits.removeLast());
            else
                unitsRemoved.add(this.buildUnits.removeFirst());
        }

        return unitsRemoved;
    }

    protected boolean isEmpty() {
        return (this.buildUnits.size() == 0);
    }

    /**
     * Works out the build time for the troops in this slot.
     * Handles if an upgrade happens for a troop by seeing when that upgrade is effective.
     * @param startFromTime
     * @return
     */
    protected long recalculateBuildUnitTimes(long startFromTime) {
        long startTime = startFromTime;
        for (BuildUnit buildUnit : this.buildUnits) {
            buildUnit.setStartTime(startTime);
            TroopData troopDataEffectiveAtStart = this.getTroopDataEffectiveAt(startTime);
            long endTime = startTime + troopDataEffectiveAtStart.getTrainingTime();
            TroopData troopDataEffectiveAtEnd = this.getTroopDataEffectiveAt(endTime);
            endTime = endTime + (troopDataEffectiveAtEnd.getTrainingTime() - troopDataEffectiveAtStart.getTrainingTime());
            buildUnit.setEndTime(endTime);
            startTime = endTime;
        }

        return startTime;
    }

    private TroopData getTroopDataEffectiveAt(long fromTime) {
        return this.playerSession.getTroopInventory().getTroopByUnitIdEffectiveFrom(this.unitId, fromTime);
    }

    public TroopData getTroopData() {
        return this.playerSession.getTroopInventory().getTroopByUnitId(this.unitId);
    }

    protected void removeBuildUnit(BuildUnit buildUnit) {
        this.buildUnits.remove(buildUnit);
    }

    protected BuildUnit getFirstBuildUnit() {
        BuildUnit buildUnit = null;
        if (buildUnits.size() > 0) {
            buildUnit = buildUnits.get(0);
        }
        return buildUnit;
    }
}
