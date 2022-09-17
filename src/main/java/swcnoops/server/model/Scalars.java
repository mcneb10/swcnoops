package swcnoops.server.model;

public class Scalars {
    public int attacksLost;
    public int attacksWon;
    public int defensesLost;
    public int defensesWon;
    public int attacksStarted;
    public int attacksCompleted;
    public int attackRating;
    public int defenseRating;
    public int xp;
    public int softCash;

    public Scalars() {
        this.attacksLost = 0;
        this.attacksWon = 0;
        this.defensesLost = 0;
        this.defensesWon = 0;
        this.attacksStarted = 0;
        this.attacksCompleted = 0;
        this.attackRating = 100;
        this.defenseRating = 0;
        this.xp = 0;
        this.softCash = 0;
    }


    public Scalars(int attacksLost, int attacksWon, int defensesLost, int defensesWon, int attacksStarted, int attacksCompleted, int attackRating, int defenseRating, int xp, int softCash) {
        this.attacksLost = attacksLost;
        this.attacksWon = attacksWon;
        this.defensesLost = defensesLost;
        this.defensesWon = defensesWon;
        this.attacksStarted = attacksStarted;
        this.attacksCompleted = attacksCompleted;
        this.attackRating = attackRating;
        this.defenseRating = defenseRating;
        this.xp = xp;
        this.softCash = softCash;
    }

    @Override
    public String toString() {
        return "Scalars{" +
                "attacksLost=" + attacksLost +
                ", attacksWon=" + attacksWon +
                ", defensesLost=" + defensesLost +
                ", defensesWon=" + defensesWon +
                ", attacksStarted=" + attacksStarted +
                ", attacksCompleted=" + attacksCompleted +
                ", attackRating=" + attackRating +
                ", defenseRating=" + defenseRating +
                ", xp=" + xp +
                ", softCash=" + softCash +
                '}';
    }
}
