package swcnoops.server.game;

public class TournamentTierData implements JoeData {
    public String uid;
    public int order;
    public int percentage;
    public String reward;
    public String points;
    public String rankName;
    public String division;
    public String divisionSmall;
    public String spriteNameEmpire;
    public String spriteNameRebel;

    @Override
    public String getUid() {
        return uid;
    }
}
