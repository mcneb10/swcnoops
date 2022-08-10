package swcnoops.server.commands.guild.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.model.TroopDonationProgress;
import swcnoops.server.session.GuildSession;

public class GuildTroopsDonateCommandResult extends GuildResult {
    private boolean reputationAwarded;
    private TroopDonationProgress troopDonationProgress;
    @JsonIgnore
    private String requestId;
    @JsonIgnore
    private String playerId;

    public GuildTroopsDonateCommandResult(GuildSession guildSession,
                                          boolean reputationAwarded,
                                          TroopDonationProgress troopDonationProgress)
    {
        super(guildSession);
        this.reputationAwarded = reputationAwarded;
        this.troopDonationProgress = troopDonationProgress;
    }

    public boolean isReputationAwarded() {
        return reputationAwarded;
    }

    public TroopDonationProgress getTroopDonationProgress() {
        return troopDonationProgress;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
