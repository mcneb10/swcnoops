package swcnoops.server.session;

import swcnoops.server.model.CurrencyType;

public class CurrencyDelta {
    private int givenDelta;
    private int expectedDelta;
    private CurrencyType currency;
    private boolean removeFromInventory;

    public CurrencyDelta(int givenDelta, int expectedDelta, CurrencyType currency, boolean removeFromInventory) {
        this.givenDelta = givenDelta;
        this.expectedDelta = expectedDelta;
        this.currency = currency;
        this.removeFromInventory = removeFromInventory;
    }

    public int getGivenDelta() {
        return givenDelta;
    }

    public int getExpectedDelta() {
        return expectedDelta;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public boolean getRemoveFromInventory() {
        return removeFromInventory;
    }

    public void rollback() {
        this.removeFromInventory = !this.removeFromInventory;
    }
}
