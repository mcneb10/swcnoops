package swcnoops.server.session;

import swcnoops.server.model.CurrencyType;

public class CurrencyDelta {
    private int givenDelta;
    private int expectedDelta;
    private CurrencyType currency;

    public CurrencyDelta(int givenDelta, int expectedDelta, CurrencyType currency) {
        this.givenDelta = givenDelta;
        this.expectedDelta = expectedDelta;
        this.currency = currency;
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
}
