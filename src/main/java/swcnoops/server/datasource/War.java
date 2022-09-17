package swcnoops.server.datasource;

import org.mongojack.Id;

public class War {
    @Id
    private String warId;
    private String squadIdA;
    private String squadIdB;
    private Long prepGraceStartTime;
    private Long prepEndTime;
    private Long actionGraceStartTime;
    private Long actionEndTime;
    private Long cooldownEndTime;
    private long processedEndTime;
    private int squadAScore;
    private int squadBScore;

    public War() {
    }

    public War(String warId, String squadIdA, String squadIdB, Long prepGraceStartTime, Long prepEndTime, Long actionGraceStartTime,
               Long actionEndTime, Long cooldownEndTime, long processedEndTime, int squadAScore, int squadBScore)
    {
        this.warId = warId;
        this.squadIdA = squadIdA;
        this.squadIdB = squadIdB;
        this.prepGraceStartTime = prepGraceStartTime;
        this.prepEndTime = prepEndTime;
        this.actionGraceStartTime = actionGraceStartTime;
        this.actionEndTime = actionEndTime;
        this.cooldownEndTime = cooldownEndTime;
        this.processedEndTime = processedEndTime;
        this.squadAScore = squadAScore;
        this.squadBScore = squadBScore;
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

    public void setPrepGraceStartTime(Long prepGraceStartTime) {
        this.prepGraceStartTime = prepGraceStartTime;
    }

    public void setPrepEndTime(Long prepEndTime) {
        this.prepEndTime = prepEndTime;
    }

    public void setActionGraceStartTime(Long actionGraceStartTime) {
        this.actionGraceStartTime = actionGraceStartTime;
    }

    public void setActionEndTime(Long actionEndTime) {
        this.actionEndTime = actionEndTime;
    }

    public void setCooldownEndTime(Long cooldownEndTime) {
        this.cooldownEndTime = cooldownEndTime;
    }

    public long getProcessedEndTime() {
        return processedEndTime;
    }

    public void setProcessedEndTime(long processedEndTime) {
        this.processedEndTime = processedEndTime;
    }

    public int getSquadAScore() {
        return squadAScore;
    }

    public int getSquadBScore() {
        return squadBScore;
    }

    public void setSquadIdA(String squadIdA) {
        this.squadIdA = squadIdA;
    }

    public void setSquadIdB(String squadIdB) {
        this.squadIdB = squadIdB;
    }

    public void setSquadAScore(int squadAScore) {
        this.squadAScore = squadAScore;
    }

    public void setSquadBScore(int squadBScore) {
        this.squadBScore = squadBScore;
    }
}
