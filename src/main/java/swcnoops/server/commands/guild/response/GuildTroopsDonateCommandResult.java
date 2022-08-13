package swcnoops.server.commands.guild.response;

import swcnoops.server.model.TroopDonationProgress;
import swcnoops.server.session.GuildSession;

import java.util.Map;

public class GuildTroopsDonateCommandResult extends GuildResult {
    private Map<String,Integer> troopsDonated;
    private boolean reputationAwarded;
    private TroopDonationProgress troopDonationProgress;

    public GuildTroopsDonateCommandResult(GuildSession guildSession, Map<String, Integer> troopsDonated,
                                          boolean reputationAwarded, TroopDonationProgress troopDonationProgress)
    {
        super(guildSession);
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
}
