package swcnoops.server.game;

import swcnoops.server.model.BattleParticipant;
import swcnoops.server.model.FactionType;

public class PvpMatch {
    private String battleId;
    private String playerId;
    private String participantId;
    private String name;
    private long battleDate;

    private FactionType factionType;
    private boolean isDevBase;
    private int level;
    private int defenderXp;


    public PvpMatch() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBattleDate() {
        return battleDate;
    }

    public void setBattleDate(long battleDate) {
        this.battleDate = battleDate;
    }

    public FactionType getFactionType() {
        return factionType;
    }

    public void setFactionType(FactionType factionType) {
        this.factionType = factionType;
    }

    public boolean isDevBase() {
        return isDevBase;
    }

    public void setDevBase(boolean devBase) {
        isDevBase = devBase;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getDefenderXp() {
        return defenderXp;
    }

    public void setDefenderXp(int defenderXp) {
        this.defenderXp = defenderXp;
    }
}



