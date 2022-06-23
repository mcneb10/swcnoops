package swcnoops.server.session;

import swcnoops.server.game.BuildableData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ContractGroup {
    final private String unitTypeId;
    final private BuildableData buildableData;
    final private LinkedList<BuildContract> contracts = new LinkedList<>();

    public ContractGroup(String unitTypeId, BuildableData buildableData) {
        this.unitTypeId = unitTypeId;
        this.buildableData = buildableData;
    }

    protected String getUnitTypeId() {
        return unitTypeId;
    }

    protected void addContractsToGroup(List<BuildContract> buildContracts) {
        for (BuildContract buildContract : buildContracts) {
            buildContract.setContractGroup(this);
            this.contracts.add(buildContract);
        }
    }

    protected List<BuildContract> removeContracts(int quantity, boolean fromBack) {
        List<BuildContract> contractsRemoved = new ArrayList<>(quantity);
        if (quantity > this.contracts.size()) {
            quantity = this.contracts.size();
        }

        for (int i = 0; i < quantity; i++) {
            if (fromBack)
                contractsRemoved.add(this.contracts.removeLast());
            else
                contractsRemoved.add(this.contracts.removeFirst());
        }

        return contractsRemoved;
    }

    protected boolean isEmpty() {
        return (this.contracts.size() == 0);
    }

    protected long recalculateContractEndTimes(long time) {
        long startTime = time;
        for (BuildContract contract : this.contracts) {
            startTime = startTime + this.buildableData.getBuildingTime();
            contract.setEndTime(startTime);
        }

        return startTime;
    }

    protected BuildableData getBuildableData() {
        return buildableData;
    }

    protected void removeCompletedContract(BuildContract troopContract) {
        this.contracts.remove(troopContract);
    }

    protected List<BuildContract> getFirstEndTime() {
        return this.contracts;
    }
}
