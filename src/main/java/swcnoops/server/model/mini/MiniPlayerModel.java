package swcnoops.server.model.mini;

import swcnoops.server.model.FactionType;
import swcnoops.server.model.GuildInfo;
import swcnoops.server.model.LeaderboardBattleHistory;
import swcnoops.server.model.PlayerMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MiniPlayerModel {
    public GuildInfo guildInfo;
    public FactionType faction;
    public PlayerMap map;
    public Map<String, LeaderboardBattleHistory> tournaments;
}
