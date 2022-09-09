package swcnoops.server.model;

import com.google.gson.annotations.SerializedName;

public class BattleParticipant {
    @SerializedName("playerId")
    public String playerId;
    @SerializedName("name")
    public String name;
    @SerializedName("guildId")
    public String guildId;
    @SerializedName("guildName")
    public String guildName;
    @SerializedName("attackRating")
    public int attackRating;
    @SerializedName("attackRatingDelta")
    public int attackRatingDelta;
    @SerializedName("defenseRating")
    public int defenseRating;
    @SerializedName("defenseRatingDelta")
    public int defenseRatingDelta;
    @SerializedName("tournamentRating")
    public int tournamentRating;
    @SerializedName("tournamentRatingDelta")
    public int tournamentRatingDelta;
    @SerializedName("faction")
    public FactionType faction;

    public BattleParticipant(){

    }

    public BattleParticipant(String playerId, String name, String guildId, String guildName, int attackRating, int attackRatingDelta, int defenseRating, int defenseRatingDelta, int tournamentRating, int tournamentRatingDelta, FactionType faction) {
        this.playerId = playerId;
        this.name = name;
        this.guildId = guildId;
        this.guildName = guildName;
        this.attackRating = attackRating;
        this.attackRatingDelta = attackRatingDelta;
        this.defenseRating = defenseRating;
        this.defenseRatingDelta = defenseRatingDelta;
        this.tournamentRating = tournamentRating;
        this.tournamentRatingDelta = tournamentRatingDelta;
        this.faction = faction;
    }
}

