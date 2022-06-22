package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildableData;
import swcnoops.server.game.GameDataManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ContractBuildQueue {
    final private LinkedList<ContractGroup> buildQueue = new LinkedList<>();
    final private Map<String,ContractGroup> buildQueueMap = new HashMap<>();

    protected LinkedList<ContractGroup> getBuildQueue() {
        return buildQueue;
    }

    protected boolean isEmpty() {
        return this.buildQueue.size() == 0;
    }

    protected void addToBuildQueue(List<AbstractBuildContract> buildContracts) {
        AbstractBuildContract buildContract = buildContracts.get(0);
        ContractGroup contractGroup = this.buildQueueMap.get(buildContract.getUnitTypeId());
        if (contractGroup == null) {
            contractGroup = createContractGroup(buildContract);
            this.buildQueueMap.put(contractGroup.getUnitTypeId(), contractGroup);
            this.buildQueue.add(contractGroup);
        }
        contractGroup.addContractsToGroup(buildContracts);
    }

    private ContractGroup createContractGroup(AbstractBuildContract abstractBuildContract) {
        BuildableData buildableData = getBuildableData(abstractBuildContract.getContractType(),
                abstractBuildContract.getUnitTypeId());
        return new ContractGroup(abstractBuildContract.getUnitTypeId(), buildableData);
    }

    private BuildableData getBuildableData(String contractType, String unitTypeId) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();

        BuildableData buildableData;
        if ("Troop".equals(contractType))
            buildableData = gameDataManager.getTroopDataByUid(unitTypeId);
        else
            throw new RuntimeException("Failed to get buildTime for " + unitTypeId);

        return buildableData;
    }

    protected void recalculateContractEndTimes(long startTime) {
        for (ContractGroup contractGroup : this.buildQueue) {
            startTime = contractGroup.recalculateContractEndTimes(startTime);
        }
    }

    protected void removeContractGroupIfEmpty(ContractGroup contractGroup) {
        if (contractGroup.isEmpty()) {
            this.buildQueue.remove(contractGroup);
            this.buildQueueMap.remove(contractGroup.getUnitTypeId());
        }
    }

    protected List<AbstractBuildContract> removeContracts(String unitTypeId, int quantity, boolean fromBack) {
        List<AbstractBuildContract> removedContracts = null;
        ContractGroup contractGroup = this.buildQueueMap.get(unitTypeId);
        if (contractGroup != null) {
            removedContracts = contractGroup.removeContracts(quantity, fromBack);
            removeContractGroupIfEmpty(contractGroup);
        }
        return removedContracts;
    }
}
