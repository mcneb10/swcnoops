package swcnoops.server.model;

public class BattleParticipant {
    public String playerId;
    public String name;
    public String guildId;
    public String guildName;
    public int attackRating;
    public int attackRatingDelta;
    public int defenseRating;
    public int defenseRatingDelta;
    public int tournamentRating;
    public int tournamentRatingDelta;
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

