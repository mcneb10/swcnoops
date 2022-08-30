package swcnoops.server.commands.player.response;

import swcnoops.server.requests.AbstractCommandResult;
import swcnoops.server.model.Attacker;
import swcnoops.server.model.Defender;
import swcnoops.server.model.Earned;
import swcnoops.server.model.Tournament;

import java.util.List;
import java.util.Map;

public class PlayerPvpBattleCompleteCommandResult extends AbstractCommandResult {
//    public BattleEntry battleEntry;
    public long attackDate;
    public Attacker attacker;
    public List<String> attackerEquipment;
    public Map<String, Integer> attackerGuildTroopsExpended;
    public long baseDamagePercent;
    public String battleId;
    public String battleVersion;
    public String cmsVersion;
    public Defender defender;
    public List<String> defenderEquipment;
    public Map<String, Integer> defenderGuildTroopsExpended;
    public long defenderPotentialMedalGain;
    public Earned earned;
    public Earned looted;
    public long manifestVersion;
    public Earned maxLootable;
    public String missionId;
    public Map<String, Integer> numVisitors;
    public String planetId;
    public long potentialMedalGain;
    public boolean revenged;
    public boolean server;
    public long stars;
    public Map<String, Integer> troopsExpended;
    public Tournament attackerTournament;
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