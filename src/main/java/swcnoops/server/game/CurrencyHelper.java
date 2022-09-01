package swcnoops.server.game;

import swcnoops.server.model.CurrencyType;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.map.MapItem;

public class CurrencyHelper {

    static public CurrencyType getCurrencyType(MapItem mapItem) {
        CurrencyType currencyType = null;
        if (mapItem != null) {
            currencyType = getCurrencyType(mapItem.getBuildingData());
        }
        return currencyType;
    }

    static public CurrencyType getCurrencyType(BuildingData buildingData) {
        CurrencyType currencyType = null;
        if (buildingData != null) {
            if (buildingData.getMaterials() != 0)
                currencyType = CurrencyType.materials;
            else if (buildingData.getCredits() != 0)
                currencyType = CurrencyType.credits;
            else if (buildingData.getContraband() != 0)
                currencyType = CurrencyType.contraband;
        }
        return currencyType;
    }

    static public int getConstructionCost(MapItem mapItem, CurrencyType currencyType) {
        int cost = 0;
        if (currencyType != null) {
            switch (currencyType) {
                case credits:
                    cost = mapItem.getBuildingData().getCredits();
                    break;
                case materials:
                    cost = mapItem.getBuildingData().getMaterials();
                    break;
                case contraband:
                    cost = mapItem.getBuildingData().getContraband();
                    break;
            }
        }

        return cost;
    }

    static public int calculateGivenConstructionCost(PlayerSession playerSession, int givenTotal,
                                               CurrencyType trainingCurrency)
    {
        int givenCost = 0;
        if (trainingCurrency != null) {
            switch (trainingCurrency) {
                case credits:
                    givenCost = playerSession.getPlayerSettings().getInventoryStorage().credits.amount - givenTotal;
                    break;
                case materials:
                    givenCost = playerSession.getPlayerSettings().getInventoryStorage().materials.amount - givenTotal;
                    break;
                case contraband:
                    givenCost = playerSession.getPlayerSettings().getInventoryStorage().contraband.amount - givenTotal;
                    break;
            }
        }

        return givenCost;
    }

    static public int getGivenTotal(CurrencyType currency, int credits, int materials, int contraband) {
        int givenTotal = 0;
        if (currency != null) {
            switch (currency) {
                case credits:
                    givenTotal = credits;
                    break;
                case materials:
                    givenTotal = materials;
                    break;
                case contraband:
                    givenTotal = contraband;
                    break;
            }
        }
        return givenTotal;
    }


    static public int calculateGivenRefund(PlayerSession playerSession, int credits, int materials, int contraband,
                                           CurrencyType currencyType)
    {
        int givenRefund = 0;
        if (currencyType != null) {
            switch (currencyType) {
                case credits:
                    givenRefund = credits - playerSession.getPlayerSettings().getInventoryStorage().credits.amount;
                    break;
                case materials:
                    givenRefund = materials - playerSession.getPlayerSettings().getInventoryStorage().materials.amount;
                    break;
                case contraband:
                    givenRefund = contraband - playerSession.getPlayerSettings().getInventoryStorage().contraband.amount;
                    break;
            }
        }

        return givenRefund;
    }
}
