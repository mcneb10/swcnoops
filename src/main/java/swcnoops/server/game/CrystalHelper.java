package swcnoops.server.game;

import swcnoops.server.ServiceFactory;
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
        return secondsToCrystals(secondsToBuy, isPrestige);
    }

    public static int secondsToCrystals(int secondsToBuy, BuildingData buildingData) {
        boolean isPrestige = buildingData.getLevel() > 10;
        return secondsToCrystals(secondsToBuy, isPrestige);
    }

    public static int calculateGivenCrystalDeltaToRemove(PlayerSession playerSession, int crystals) {
        int givenCrystalsDelta = playerSession.getPlayerSettings().getInventoryStorage().crystals.amount - crystals;
        return givenCrystalsDelta;
    }
}
