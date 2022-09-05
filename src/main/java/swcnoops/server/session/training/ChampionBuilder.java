package swcnoops.server.session.training;

import swcnoops.server.game.ContractType;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.map.MapItem;

import java.util.List;

public class ChampionBuilder extends Builder {
    public ChampionBuilder(PlayerSession playerSession, MapItem mapItem, DeployableQueue deployableQueue, ContractType contractType) {
        super(playerSession, mapItem, deployableQueue, contractType);
    }

    @Override
    protected void train(List<BuildUnit> buildUnits, long startTime) {
        // there can only ever be one contract in here so we clear the last encase we are out of sync
        this.buildQueue.getBuildQueue().clear();
        this.startTime = startTime;

        this.buildQueue.add(buildUnits.get(0));
        this.recalculateBuildUnitTimes(startTime);

        DeployableQueue transport = this.getDeployableQueue();
        if (transport != null) {
            // we force the queue to only have 1 for this unit
            transport.getUnitsInQueue().removeIf(a -> a.getUnitId().equals(buildUnits.get(0).getUnitId()));
            transport.getDeployableUnits().remove(buildUnits.get(0).getUnitId());
            transport.addUnitsToQueue(buildUnits.get(0));
            transport.sortUnitsInQueue();
        }
    }

    @Override
    protected List<BuildUnit> remove(String unitTypeId, int quantity, long time, boolean isBuyout) {
        // force only deka being repaired
        List<BuildUnit> removed = this.buildQueue.removeAll(unitTypeId);
        this.recalculateBuildUnitTimes(time);

        DeployableQueue transport = this.getDeployableQueue();
        if (transport != null) {
            if (!isBuyout) {
                transport.removeUnitsFromQueue(removed);
            } else {
                transport.moveUnitToDeployable(removed);
            }
            transport.sortUnitsInQueue();
        }

        return removed;
    }
}
