package swcnoops.server.game;

import swcnoops.server.model.BattleParticipant;

public class PvpMatch {
    private String battleId;
    private String playerId;
    private String participantId;

    private long battleDate;
    private BattleParticipant attacker;

    private BattleParticipant defender;

    public PvpMatch(String battleId, String playerId, String participantId, BattleParticipant attacker, BattleParticipant defender, long battleDate) {
        this.battleId = battleId;
        this.playerId = playerId;
        this.participantId = participantId;
        this.attacker = attacker;
        this.defender = defender;
        this.battleDate = battleDate;
    }

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public BattleParticipant getAttacker() {
        return attacker;
    }

    public BattleParticipant getDefender() {
        return defender;
    }

    public long getBattleDate() {
        return battleDate;
    }
}

