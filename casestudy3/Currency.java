package casestudy3;

public enum Currency {
    INR(2),
    USD(2),
    EUR(2),
    GBP(2);

    private final int minorUnits;

    Currency(int minorUnits) {
        this.minorUnits = minorUnits;
    }

    public int getMinorUnits() {
        return minorUnits;
    }
}
