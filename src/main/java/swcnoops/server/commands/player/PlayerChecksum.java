package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.requests.CommandResult;

abstract public class PlayerChecksum<A extends PlayerChecksum, B extends CommandResult> extends AbstractCommandAction<A,B> {
    private int credits;
    private int materials;
    private int contraband;
    private int crystals;
    private long resourceChecksum;
    private long infoChecksum;
    //private PlayerBuildingContract additionalContract;

    public int getCredits() {
        return credits;
    }

    public int getMaterials() {
        return materials;
    }

    public int getContraband() {
        return contraband;
    }

    public int getCrystals() {
        return crystals;
    }

    public long getResourceChecksum() {
        return resourceChecksum;
    }

    public long getInfoChecksum() {
        return infoChecksum;
    }
}
