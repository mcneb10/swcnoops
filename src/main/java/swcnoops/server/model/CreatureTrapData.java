package swcnoops.server.model;

public class CreatureTrapData {
    public String buildingId;
    public String championUid;
    /**
     * This sets the creature as expended or not.
     * Does not control the creature trap, just if the creature can be deployed for war.
     */
    public boolean ready;
    public String specialAttackUid;
}
