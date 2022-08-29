package swcnoops.server.model;

public class ShareBattleNotificationData implements SquadNotificationData {
    private String battleId;
    private String battleVersion;
    private String cmsVersion;
    private SquadBattleReplayType type;
    private int battleScoreDelta;
    private int damagePercent;
    private int stars;
    private String opponentId;
    private String opponentName;
    private FactionType opponentFaction;
    private FactionType faction;

    public ShareBattleNotificationData() {
    }

    public ShareBattleNotificationData(String battleId) {
        this.battleId = battleId;
    }

    public void setBattleVersion(String battleVersion) {
        this.battleVersion = battleVersion;
    }

    public void setCmsVersion(String cmsVersion) {
        this.cmsVersion = cmsVersion;
    }

    public void setType(SquadBattleReplayType type) {
        this.type = type;
    }

    public void setBattleScoreDelta(int battleScoreDelta) {
        this.battleScoreDelta = battleScoreDelta;
    }

    public void setDamagePercent(int damagePercent) {
        this.damagePercent = damagePercent;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setOpponentId(String opponentId) {
        this.opponentId = opponentId;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public void setOpponentFaction(FactionType opponentFaction) {
        this.opponentFaction = opponentFaction;
    }

    public void setFaction(FactionType faction) {
        this.faction = faction;
    }

    public String getBattleId() {
        return battleId;
    }

    public String getBattleVersion() {
        return battleVersion;
    }

    public String getCmsVersion() {
        return cmsVersion;
    }

    public SquadBattleReplayType getType() {
        return type;
    }

    public int getBattleScoreDelta() {
        return battleScoreDelta;
    }

    public int getDamagePercent() {
        return damagePercent;
    }

    public int getStars() {
        return stars;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public FactionType getOpponentFaction() {
        return opponentFaction;
    }

    public FactionType getFaction() {
        return faction;
    }
}
