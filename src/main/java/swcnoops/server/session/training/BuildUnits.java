package swcnoops.server.session.training;

import swcnoops.server.game.ContractType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Using this to make json parsing work for this collection
 */
public class BuildUnits extends ArrayList<BuildUnit> {
    public Set<String> getNewBuildContractKeys() {
        Set<String> newBuilds = new HashSet<>();
        this.stream().filter(a -> a.getContractType() == ContractType.Build).forEach(a -> newBuilds.add(a.getBuildingId()));
        return newBuilds;
    }
}
