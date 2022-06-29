package swcnoops.server.commands.guild.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.model.TroopDonationData;
import swcnoops.server.model.TroopDonationProgress;
import swcnoops.server.requests.AbstractCommandResult;
import swcnoops.server.session.GuildSession;

import java.util.Map;

public class GuildTroopsDonateCommandResult extends AbstractCommandResult {
    private Map<String,Integer> troopsDonated;
    private boolean reputationAwarded;
    private TroopDonationProgress troopDonationProgress;
    @JsonIgnore
    private String requestId;
    @JsonIgnore
    private String playerId;
    @JsonIgnore
    private TroopDonationData troopDonationData;
    @JsonIgnore
    private GuildSession guildSession;
    private String name;

    public GuildTroopsDonateCommandResult(Map<String, Integer> troopsDonated, boolean reputationAwarded,
                                          TroopDonationProgress troopDonationProgress)
    {
        this.troopsDonated = troopsDonated;
        this.reputationAwarded = reputationAwarded;
        this.troopDonationProgress = troopDonationProgress;
    }

    public Map<String, Integer> getTroopsDonated() {
        return troopsDonated;
    }

    public boolean isReputationAwarded() {
        return reputationAwarded;
    }

    public TroopDonationProgress getTroopDonationProgress() {
        return troopDonationProgress;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setTroopDonationData(TroopDonationData troopDonationData) {
        this.troopDonationData = troopDonationData;
    }

    public TroopDonationData getTroopDonationData() {
        return troopDonationData;
    }

    public void setGuildSession(GuildSession guildSession) {
        this.guildSession = guildSession;
    }

    public GuildSession getGuildSession() {
        return guildSession;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
