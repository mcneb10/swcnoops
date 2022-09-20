package swcnoops.server.game;

import swcnoops.server.ServiceFactory;
import swcnoops.server.model.InventoryStorage;
import swcnoops.server.model.TroopType;
import swcnoops.server.session.PlayerSession;

public class CrystalHelper {
    private static int secondsToCrystals(int seconds, boolean prestige)
    {
        GameConstants constants = ServiceFactory.instance().getGameDataManager().getGameConstants();
        float baseValue = (float)seconds / 3600f;
        int coefficient = (!prestige) ? constants.crystals_speed_up_coefficient : constants.crystals_speed_up_prestige_coefficient;
        int exponent = (!prestige) ? constants.crystals_speed_up_exponent : constants.crystals_speed_up_prestige_exponent;
        return currencyPow(baseValue, coefficient, exponent, constants);
    }

    static private int currencyPow(float baseValue, int coefficient, int exponent, GameConstants constants)
    {
        int coef_EXP_ACCURACY = constants.coef_exp_accuracy;
        if (baseValue < 0f || coefficient <= 0 || exponent <= 0 || coef_EXP_ACCURACY <= 0)
        {
            return -1;
        }
        float num = (float)coefficient / (float)coef_EXP_ACCURACY;
        float p = (float)exponent / (float)coef_EXP_ACCURACY;
        return (int)Math.ceil(num * Math.pow(baseValue, p));
    }

    public static int secondsToCrystals(int secondsToBuy, TroopData troopData) {
        boolean isPrestige = troopData.getLevel() > 10;
        if (troopData.getType() == TroopType.champion)
            isPrestige = false;

        return secondsToCrystals(secondsToBuy, isPrestige);
    }

    public static int secondsToCrystalsForResearch(int secondsToBuy) {
        return secondsToCrystals(secondsToBuy, false);
    }

    public static int secondsToCrystals(int secondsToBuy, BuildingData buildingData) {
        boolean isPrestige = buildingData.getPrestige();
        return secondsToCrystals(secondsToBuy, isPrestige);
    }

    public static int calculateGivenCrystalDeltaToRemove(PlayerSession playerSession, int crystals) {
        InventoryStorage inventoryStorage = playerSession.getInventoryStorage();
        int givenCrystalsDelta = inventoryStorage.crystals.amount - crystals;
        return givenCrystalsDelta;
    }

    public static int calculateGivenCrystalDeltaToAdd(PlayerSession playerSession, int crystals) {
        InventoryStorage inventoryStorage = playerSession.getInventoryStorage();
        int givenCrystalsDelta = crystals - inventoryStorage.crystals.amount;
        return givenCrystalsDelta;
    }

    public static int crystalCostToUpgradeAllWalls(int oneWallCost, int numWalls)
    {
        GameConstants constants = ServiceFactory.instance().getGameDataManager().getGameConstants();
        int num = oneWallCost * numWalls;
        int upgrade_ALL_WALLS_COEFFICIENT = constants.upgrade_all_walls_coefficient;
        int upgrade_ALL_WALL_EXPONENT = constants.upgrade_all_wall_exponent;
        int num2 = currencyPow((float)num, upgrade_ALL_WALLS_COEFFICIENT, upgrade_ALL_WALL_EXPONENT, constants);
        return (int)Math.ceil((float)num2 * constants.upgrade_all_walls_convenience_tax);
    }

    public static int creditsCrystalCost(int credits)
    {
        GameConstants constants = ServiceFactory.instance().getGameDataManager().getGameConstants();
        int credits_COEFFICIENT = constants.credits_coefficient;
        int credits_EXPONENT = constants.credits_exponent;
        int num = currencyPow((float)credits, credits_COEFFICIENT, credits_EXPONENT, constants);
//        if (applySale)
//        {
//            int credits_LEVER_PERCENTAGE = GameConstants.CREDITS_LEVER_PERCENTAGE;
//            num = GameUtils.ScaleByPercentage(num, credits_LEVER_PERCENTAGE);
//        }
        return num;
    }
}
