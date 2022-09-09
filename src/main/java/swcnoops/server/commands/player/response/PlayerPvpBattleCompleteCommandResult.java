package swcnoops.server.commands.player.response;

import swcnoops.server.model.*;
import swcnoops.server.requests.AbstractCommandResult;

import java.util.List;
import java.util.Map;

public class PlayerPvpBattleCompleteCommandResult extends AbstractCommandResult {
    //    public BattleEntry battleEntry;
    public String battleVersion;
    public String cmsVersion;
    public String missionId;
    public long stars;
    public long baseDamagePercent;
    public String manifestVersion;
    public String battleId;
    public BattleParticipant attacker;
    public BattleParticipant defender;
    public long attackDate;
    public String planetId;
    public long potentialMedalGain;

    public Map<String, Integer> troopsExpended;
    public Map<String, Integer> attackerGuildTroopsExpended;

    public Earned looted;
    public Earned earned;
    public Earned maxLootable;

    public boolean revenged;

//    public String defenseEncounterProfile;
//    public String battleScript; - possible this is to do with testing on SWC Devs side, ie script a battle so it doesn't need to be manually tested??


    public List<String> attackerEquipment;
    public List<String> defenderEquipment;
    public Map<String, Integer> defenderGuildTroopsExpended;
    public long defenderPotentialMedalGain;
    public Map<String, Integer> numVisitors;
    public boolean server;
    public Tournament attackerTournament = new Tournament();
}

/*
        "tournaments": {
            "conflict_erk_20190523": {
                "attacksLost": 5,
                "attacksWon": 0,
                "bestTier": 8,
                "collected": true,
                "defensesLost": 1,
                "defensesWon": 0,
                "donatedTroops": 2,
                "percentile": 10.367303796353387,
                "rating": 0,
                "redeemedRewards": [
                    "lc_conf_tier1"
                ],
                "tier": "tournament_tier_1",
                "uid": "conflict_erk_20190523"
            }
        }
 */