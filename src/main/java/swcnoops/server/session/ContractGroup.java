package swcnoops.server.session;

import swcnoops.server.game.BuildableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ContractGroup {
    final private String unitTypeId;
    final private BuildableData buildableData;
    private long startTime;
    private boolean headRemoved = false;
    final private LinkedList<AbstractBuildContract> contracts = new LinkedList<>();
    public ContractGroup(String unitTypeId, BuildableData buildableData, long startTime) {
        this.unitTypeId = unitTypeId;
        this.buildableData = buildableData;
        this.startTime = startTime;
    }

    public String getUnitTypeId() {
        return unitTypeId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void addContractsToGroup(List<AbstractBuildContract> buildContracts) {
        for (AbstractBuildContract buildContract : buildContracts) {
            buildContract.setContractGroup(this);
            this.contracts.add(buildContract);
        }
    }

    public void removeContracts(int quantity) {
        if (this.contracts.size() > quantity) {
            quantity = this.contracts.size();
        }

        for (int i = 0; i < quantity; i++) {
            this.contracts.removeLast();
        }
    }

    public boolean isEmpty() {
        return (this.contracts.size() == 0);
    }

    public long recalculateContractEndTimes(long time) {
        long startTime = time;
        for (AbstractBuildContract contract : contracts) {
            startTime = startTime + this.buildableData.getBuildingTime();
            contract.setEndTime(startTime);
        }

        return startTime;
    }

    public BuildableData getBuildableData() {
        return buildableData;
    }

    public void removeCompletedContract(AbstractBuildContract troopContract) {
        // this should really always be the first in the queue
        this.headRemoved = true;
        this.contracts.remove(troopContract);
    }

    public boolean isHeadRemoved() {
        return headRemoved;
    }

    public void resetHeadRemoved(long time) {
        this.setHeadRemoved(false);
        this.startTime = time;
    }

    public void setHeadRemoved(boolean headRemoved) {
        this.headRemoved = headRemoved;
    }

    public List<AbstractBuildContract> buyOutContract(int quantity) {
        List<AbstractBuildContract> boughtOutContracts = new ArrayList<>(quantity);
        Iterator<AbstractBuildContract> contractIterator = this.contracts.iterator();

        while (contractIterator.hasNext() && quantity > 0) {
            quantity--;
            AbstractBuildContract buildContract = contractIterator.next();
            boughtOutContracts.add(buildContract);
            contractIterator.remove();
            this.headRemoved = true;
        }

        return boughtOutContracts;
    }
}
