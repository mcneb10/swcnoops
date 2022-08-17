package swcnoops.server.datasource;

public class War {
    private String squadIdA;
    private String squadIdB;
    private Long prepGraceStartTime;
    private Long prepEndTime;
    private Long actionGraceStartTime;
    private Long actionEndTime;
    private Long cooldownEndTime;
    private String warId;

    public War(String warId, String squadIdA, String squadIdB, Long prepGraceStartTime, Long prepEndTime, Long actionGraceStartTime,
               Long actionEndTime, Long cooldownEndTime)
    {
        this.warId = warId;
        this.squadIdA = squadIdA;
        this.squadIdB = squadIdB;
        this.prepGraceStartTime = prepGraceStartTime;
        this.prepEndTime = prepEndTime;
        this.actionGraceStartTime = actionGraceStartTime;
        this.actionEndTime = actionEndTime;
        this.cooldownEndTime = cooldownEndTime;
    }

    public String getSquadIdA() {
        return squadIdA;
    }

    public String getSquadIdB() {
        return squadIdB;
    }

    public Long getPrepGraceStartTime() {
        return prepGraceStartTime;
    }

    public Long getPrepEndTime() {
        return prepEndTime;
    }

    public Long getActionGraceStartTime() {
        return actionGraceStartTime;
    }

    public Long getActionEndTime() {
        return actionEndTime;
    }

    public Long getCooldownEndTime() {
        return cooldownEndTime;
    }

    public String getWarId() {
        return warId;
    }

    public void setWarId(String warId) {
        this.warId = warId;
    }
}
