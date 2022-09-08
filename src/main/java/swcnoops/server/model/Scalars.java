package swcnoops.server.model;

import com.google.gson.annotations.SerializedName;

public class Scalars {
    @SerializedName("attacksLost")
    public int attacksLost;
    @SerializedName("attacksWon")
    public int attacksWon;
    @SerializedName("defensesLost")
    public int defensesLost;
    @SerializedName("defensesWon")
    public int defensesWon;
    @SerializedName("attacksStarted")
    public int attacksStarted;
    @SerializedName("attacksCompleted")
    public int attacksCompleted;
    @SerializedName("attackRating")
    public int attackRating;
    @SerializedName("defenseRating")
    public int defenseRating;
    @SerializedName("xp")
    public int xp;
    @SerializedName("softCash")
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
