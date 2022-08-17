package swcnoops.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.session.PlayerSession;

public class Member {
    public int attacksWon;
    public int defensesWon;
    public boolean hasPlanetaryCommand;
    private int hqLevel;
    public boolean isOfficer;
    public boolean isOwner;
    public long joinDate;
    public long lastLoginTime;
    public long lastUpdated;
    public String name;
    public String planet;
    public String playerId;
    public int rank;
    public int reputationInvested;
    public long score;
    public int tournamentRating;
    public TournamentScores tournamentScores;
    public long troopsDonated;
    public long troopsReceived;
    public int warParty;
    public int xp;

    @JsonIgnore
    final private PlayerSession playerSession;

    public Member() {
        this(null);
    }

    public Member(PlayerSession playerSession) {
        this.playerSession = playerSession;
        if (this.playerSession != null) {
            this.playerId = playerSession.getPlayerId();
            this.planet = playerSession.getPlayerSettings().getBaseMap().planet;
            this.name = playerSession.getPlayerSettings().getName();
        }
    }

    public void setIsOfficer(boolean isOfficer) {
        this.isOfficer = isOfficer;
    }

    public int getHqLevel() {
        if (hasPlayerSession())
            return this.playerSession.getHeadQuarter().getBuildingData().getLevel();

        return hqLevel;
    }

    @JsonIgnore
    public boolean hasPlayerSession() {
        return (this.playerSession != null);
    }

    public void setLevel(int hqLevel) {
        this.hqLevel = hqLevel;
    }
}
