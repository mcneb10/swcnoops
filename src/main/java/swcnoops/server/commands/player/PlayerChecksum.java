package swcnoops.server.commands.player;

import com.fasterxml.jackson.annotation.JsonProperty;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.requests.CommandResult;

abstract public class PlayerChecksum<A extends PlayerChecksum, B extends CommandResult> extends AbstractCommandAction<A,B> {
    @JsonProperty("_credits")
    private int credits;
    @JsonProperty("_materials")
    private int materials;
    @JsonProperty("_contraband")
    private int contraband;
    @JsonProperty("_crystals")
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
