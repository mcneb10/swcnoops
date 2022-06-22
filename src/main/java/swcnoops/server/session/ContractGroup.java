package swcnoops.server.session;

import swcnoops.server.game.BuildableData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ContractGroup {
    final private String unitTypeId;
    final private BuildableData buildableData;
    final private LinkedList<AbstractBuildContract> contracts = new LinkedList<>();

    public ContractGroup(String unitTypeId, BuildableData buildableData) {
        this.unitTypeId = unitTypeId;
        this.buildableData = buildableData;
    }

    protected String getUnitTypeId() {
        return unitTypeId;
    }

    protected void addContractsToGroup(List<AbstractBuildContract> buildContracts) {
        for (AbstractBuildContract buildContract : buildContracts) {
            buildContract.setContractGroup(this);
            this.contracts.add(buildContract);
        }
    }

    protected List<AbstractBuildContract> removeContracts(int quantity, boolean fromBack) {
        List<AbstractBuildContract> contractsRemoved = new ArrayList<>(quantity);
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
        for (AbstractBuildContract contract : contracts) {
            startTime = startTime + this.buildableData.getBuildingTime();
            contract.setEndTime(startTime);
        }

        return startTime;
    }

    protected BuildableData getBuildableData() {
        return buildableData;
    }

    protected void removeCompletedContract(AbstractBuildContract troopContract) {
        this.contracts.remove(troopContract);
    }

    protected List<AbstractBuildContract> getFirstEndTime() {
        return this.contracts;
    }
}
