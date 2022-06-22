package swcnoops.server.session;

public class TroopBuildContract extends AbstractBuildContract {
    public TroopBuildContract(String buildingId, String unitTypeId, ContractConstructor parent) {
        super(buildingId, unitTypeId, parent);
    }

    @Override
    final public String getContractType() {
        return "Troop";
    }
}
