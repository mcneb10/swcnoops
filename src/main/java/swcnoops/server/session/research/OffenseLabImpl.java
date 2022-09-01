package swcnoops.server.session.research;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.TroopData;
import swcnoops.server.model.Building;
import swcnoops.server.model.Position;
import swcnoops.server.session.CurrencyDelta;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.inventory.TroopUpgrade;
import swcnoops.server.session.inventory.Troops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OffenseLabImpl implements OffenseLab {
    final private Building building;
    private BuildingData buildingData;
    final private PlayerSession playerSession;
    public OffenseLabImpl(PlayerSession playerSession, Building building, BuildingData buildingData) {
        this.playerSession = playerSession;
        this.building = building;
        this.buildingData = buildingData;
    }

    @Override
    public String getBuildingKey() {
        return building.key;
    }

    @Override
    public String getBuildingUid() {
        return building.uid;
    }


    @Override
    public Building getBuilding() {
        return this.building;
    }

    @Override
    public BuildingData getBuildingData() {
        return buildingData;
    }

    public void setBuildingData(BuildingData buildingData) {
        this.buildingData = buildingData;
    }

    @Override
    public void buyout(long time) {
        // can only really handle 1 upgrade at a time
        if (isResearchingTroop()) {
            Troops troops = this.playerSession.getTroopInventory().getTroops();
            List<TroopUpgrade> buyoutList = new ArrayList<>(troops.getUpgrades());
            for (TroopUpgrade troopUpgrade : buyoutList) {
                troopUpgrade.buyout(time);
                this.processCompletedUpgrades(time);
            }
        }
    }

    @Override
    public CurrencyDelta cancel(long time, int credits, int materials, int contraband) {
        if (isResearchingTroop()) {
            Troops troops = this.playerSession.getTroopInventory().getTroops();
            troops.getUpgrades().clear();
        }

        return null;
    }

    @Override
    public void upgradeStart(String buildingId, String troopUid, long time) {
        if (this.playerSession.getTroopInventory().getTroops() != null) {
            TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(troopUid);
            long endTime = time + troopData.getUpgradeTime();
            TroopUpgrade troopUpgrade = new TroopUpgrade(buildingId, troopUid, endTime);
            this.playerSession.getTroopInventory().getTroops().getUpgrades().add(troopUpgrade);
        }
    }

    @Override
    public boolean processCompletedUpgrades(long time) {
        boolean hasUpgrade = false;

        Troops troops = this.playerSession.getTroopInventory().getTroops();
        Iterator<TroopUpgrade> troopUpgradeIterator = troops.getUpgrades().iterator();
        while (troopUpgradeIterator.hasNext()) {
            TroopUpgrade troopUpgrade = troopUpgradeIterator.next();
            if (troopUpgrade.getEndTime() <= time) {
                troopUpgradeIterator.remove();
                TroopData troopData = ServiceFactory.instance().getGameDataManager()
                        .getTroopDataByUid(troopUpgrade.getTroopUnitId());
                this.playerSession.getTroopInventory().upgradeTroop(troopData, time);
                hasUpgrade = true;
            }
        }

        return hasUpgrade;
    }

    @Override
    public boolean isResearchingTroop() {
        Troops troops = this.playerSession.getTroopInventory().getTroops();
        return troops.getUpgrades().size() > 0;
    }

    @Override
    public void changeBuildingData(BuildingData buildingData) {
        throw new NotImplementedException();
    }

    @Override
    public void upgradeComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        throw new NotImplementedException();
    }

    @Override
    public void moveTo(Position newPosition) {
        throw new NotImplementedException();
    }

    @Override
    public CurrencyDelta collect(PlayerSession playerSession, int credits, int materials, int contraband, int crystals, long time) {
        return null;
    }

    @Override
    public void buildComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
    }
}
