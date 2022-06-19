package swcnoops.server.commands.player.response;

import swcnoops.server.model.PlayerMap;
import swcnoops.server.requests.AbstractCommandResult;

public class PlayerPvpGetNextTargetCommandResult extends AbstractCommandResult {
    public String battleId;
    public String playerId;
    public String name;
    public int level;
    public int xp;
    public String faction;
    public int attackRating;
    public int defenseRating;
    public int attacksWon;
    public int defensesWon;
    public String guildId;
    public String guildName;
    public Object guildDonatedTroops;
    public Object champions;
    public int potentialMedalsToGain;
    public int potentialMedalsToLose;
    public int potentialTournamentRatingDeltaWin;
    public int potentialTournamentRatingDeltaLose;
    public PlayerMap map;
    public int availableCredits;
    public int availableMaterials;
    public int availableContraband;
    public Object buildingLootCreditsMap;
    public Object buildingLootMaterialsMap;
    public Object buildingLootContrabandMap;
    public int creditsCharged;
    public Object contracts;
    public Object equipment;
    public Object attackerDeployables;
}
