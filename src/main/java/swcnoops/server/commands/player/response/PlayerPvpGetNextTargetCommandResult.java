package swcnoops.server.commands.player.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.model.*;
import swcnoops.server.requests.AbstractCommandResult;
import swcnoops.server.requests.ResponseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerPvpGetNextTargetCommandResult extends AbstractCommandResult {
    public String battleId;
    public String playerId;
    public String name;
    public int level;
    public int xp;
    public FactionType faction;

    public int attackRating;
    public int defenseRating;
    public int attacksWon;
    public int defensesWon;

    public String guildId;
    public String guildName;

    public PlayerMap map;
    public DonatedTroops guildTroops;
    public Map<String, Map<CurrencyType, Integer>> resources;

    public Map<String, Integer> champions;
    public List<CreatureTrapData> creatureTrapData;

    public Map<String, Integer> potentialPoints;

//    public int potentialMedalsToGain;
//    public int potentialMedalsToLose;
    //    public int potentialTournamentRatingDeltaWin;
//    public int potentialTournamentRatingDeltaLose;
//    public int availableCredits;
//    public int availableMaterials;
//    public int availableContraband;
//    public Object buildingLootCreditsMap;
//    public Object buildingLootMaterialsMap;
//    public Object buildingLootContrabandMap;
//    public Object attackerDeployables;
    public int creditsCharged;
    public Object contracts;
    public List<String> equipment = new ArrayList<>();

    @JsonIgnore
    private int status = ResponseHelper.RECEIPT_STATUS_COMPLETE;

    public PlayerPvpGetNextTargetCommandResult() {
    }

    public PlayerPvpGetNextTargetCommandResult(int statusCodePvpTargetNotFound) {
        this.status = statusCodePvpTargetNotFound;
    }

    @Override
    public Integer getStatus() {
        return this.status;
    }
}
