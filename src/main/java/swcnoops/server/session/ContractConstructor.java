package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildableData;
import swcnoops.server.game.GameDataManager;
import java.util.*;

/**
 * This class represents one of the buildings on the map used to build something.
 * It simulates the behavior of what you see in the game.
 * This is so the server can keep the data in sync when the client reloads.
 * Each unit type being built is grouped in the game, the order of the build of a single unit
 * is dependent on the order of its group, and not when the unit was selected to be built.
 */
public class ContractConstructor {
    final private String buildingId;
    final private LinkedList<ContractGroup> buildQueue = new LinkedList<>();
    final private Map<String,ContractGroup> buildQueueMap = new HashMap<>();

    public ContractConstructor(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingId() {
        return this.buildingId;
    }

    public void addContracts(List<AbstractBuildContract> buildContracts, long startTime) {
        AbstractBuildContract buildContract = buildContracts.get(0);
        ContractGroup contractGroup = this.buildQueueMap.get(buildContract.getUnitTypeId());
        if (contractGroup == null) {
            contractGroup = createContractGroup(buildContract, startTime);
            this.buildQueueMap.put(contractGroup.getUnitTypeId(), contractGroup);
            this.buildQueue.add(contractGroup);
        }

        contractGroup.addContractsToGroup(buildContracts);
    }

    private ContractGroup createContractGroup(AbstractBuildContract abstractBuildContract, long startTime) {
        BuildableData buildableData = getBuildableData(abstractBuildContract.getContractType(), abstractBuildContract.getUnitTypeId());
        ContractGroup contractGroup = new ContractGroup(abstractBuildContract.getUnitTypeId(), buildableData, startTime);
        return contractGroup;
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

    public void cancelContract(String unitTypeId, int quantity) {
        ContractGroup contractGroup = this.buildQueueMap.get(unitTypeId);
        if (contractGroup != null) {
            boolean groupHeadOfQueue = this.buildQueue.getFirst() == contractGroup;
            contractGroup.removeContracts(quantity);
            if (removeContractGroupIfEmpty(contractGroup)) {
                if (groupHeadOfQueue && this.buildQueue.size() > 0)
                    this.buildQueue.getFirst().setHeadRemoved(true);
            }
        } else {
            throw new RuntimeException("Failed to find unit to remove " + unitTypeId);
        }
    }

    public boolean removeContractGroupIfEmpty(ContractGroup contractGroup) {
        boolean removed = false;
        if (contractGroup.isEmpty()) {
            this.buildQueue.remove(contractGroup);
            this.buildQueueMap.remove(contractGroup.getUnitTypeId());
            removed = true;
        }

        return removed;
    }

    public void recalculateContractEndTimes(long time) {
        long startTime = time;
        if (this.buildQueue.size() > 0) {
            ContractGroup firstContractGroup = this.buildQueue.getFirst();
            if (firstContractGroup.isHeadRemoved())
                firstContractGroup.resetHeadRemoved(time);
            startTime = firstContractGroup.getStartTime();
        }

        for (ContractGroup contractGroup : this.buildQueue) {
            startTime = contractGroup.recalculateContractEndTimes(startTime);
        }
    }

    public List<AbstractBuildContract> buyOutContract(String unitTypeId, int quantity, long time) {
        List<AbstractBuildContract> boughtOutContracts;
        ContractGroup contractGroup = this.buildQueueMap.get(unitTypeId);
        if (contractGroup != null) {
            boughtOutContracts = contractGroup.buyOutContract(quantity);
            boolean groupHeadOfQueue = this.buildQueue.getFirst() == contractGroup;
            if (this.removeContractGroupIfEmpty(contractGroup)) {
                if (groupHeadOfQueue && this.buildQueue.size() > 0)
                    this.buildQueue.getFirst().setHeadRemoved(true);
            }
        } else {
            throw new RuntimeException("Failed to find unit to remove " + unitTypeId);
        }

        return boughtOutContracts;
    }
}
