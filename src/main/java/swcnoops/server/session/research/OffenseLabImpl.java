package swcnoops.server.session.research;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.TroopData;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.inventory.TroopRecord;
import swcnoops.server.session.inventory.TroopUpgrade;
import swcnoops.server.session.inventory.Troops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OffenseLabImpl implements OffenseLab {
    final private String buildingId;
    private BuildingData buildingData;
    final private PlayerSession playerSession;
    public OffenseLabImpl(PlayerSession playerSession, String buildingId, BuildingData buildingData) {
        this.playerSession = playerSession;
        this.buildingId = buildingId;
        this.buildingData = buildingData;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public BuildingData getBuildingData() {
        return buildingData;
    }

    public void setBuildingData(BuildingData buildingData) {
        this.buildingData = buildingData;
    }

    @Override
    public void buyout(long time) {
        Troops troops = this.playerSession.getTroopInventory().getTroops();

        // can only really handle 1 upgrade at a time
        if (troops.getUpgrades().size() > 0) {
            List<TroopUpgrade> buyoutList = new ArrayList<>(troops.getUpgrades());
            for (TroopUpgrade troopUpgrade : buyoutList) {
                troopUpgrade.buyout(time);
                this.processCompletedUpgrades(time);
            }
        }
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
        while(troopUpgradeIterator.hasNext()) {
            TroopUpgrade troopUpgrade = troopUpgradeIterator.next();
            if (troopUpgrade.getEndTime() <= time) {
                troopUpgradeIterator.remove();
                TroopData troopData = ServiceFactory.instance().getGameDataManager()
                        .getTroopDataByUid(troopUpgrade.getTroopUnitId());
                TroopRecord troopRecord = new TroopRecord(troopData.getLevel(), time);
                if (troopData.isSpecialAttack())
                    troops.getSpecialAttacks().put(troopData.getUnitId(), troopRecord);
                else
                    troops.getTroops().put(troopData.getUnitId(), troopRecord);

                // upgrade the players inventory, and our record of when the upgrade becomes effective
                troops.getTroopRecords().put(troopData.getUnitId(), troopRecord);
                this.playerSession.getTroopInventory().upgradeTroop(troopData);
                hasUpgrade = true;
            }
        }

        return hasUpgrade;
    }
}
