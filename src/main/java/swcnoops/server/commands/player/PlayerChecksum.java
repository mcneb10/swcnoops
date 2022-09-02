package swcnoops.server.commands.player;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseData;
import swcnoops.server.session.PlayerSession;

abstract public class PlayerChecksum<A extends PlayerChecksum, B extends CommandResult> extends AbstractCommandAction<A,B> {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerChecksum.class);

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

    @Override
    public ResponseData execute(Command command) throws Exception {
        JsonParser jsonParser = ServiceFactory.instance().getJsonParser();
        A parsedArgument = this.parseArgument(jsonParser, command.getArgs());
        command.setParsedArgument(parsedArgument);
        B commandResult = this.execute(parsedArgument, command.getTime());
        command.setResponse(commandResult);
        ResponseData responseData = createResponse(command, commandResult);

        // we do a simple check here to see what the client thinks it has compared to the server
        if (doResourceSyncCheck())
            checkIfInSync(command.getAction(), parsedArgument);
        return responseData;
    }

    private void checkIfInSync(String action, A parsedArgument) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(parsedArgument.getPlayerId());

        if (playerSession != null) {
            boolean acceptedClientValues = false;
            if (acceptCredits() && playerSession.getPlayerSettings().getInventoryStorage().credits.amount != parsedArgument.getCredits()) {
                playerSession.getPlayerSettings().getInventoryStorage().credits.amount = parsedArgument.getCredits();
                acceptedClientValues = true;
            }

            if (acceptMaterials() && playerSession.getPlayerSettings().getInventoryStorage().materials.amount != parsedArgument.getMaterials()) {
                playerSession.getPlayerSettings().getInventoryStorage().materials.amount = parsedArgument.getMaterials();
                acceptedClientValues = true;
            }

            if (acceptContraband() && playerSession.getPlayerSettings().getInventoryStorage().contraband.amount != parsedArgument.getContraband()) {
                playerSession.getPlayerSettings().getInventoryStorage().contraband.amount = parsedArgument.getContraband();
                acceptedClientValues = true;
            }

            if (acceptCrystals() && playerSession.getPlayerSettings().getInventoryStorage().crystals.amount != parsedArgument.getCrystals()) {
                playerSession.getPlayerSettings().getInventoryStorage().crystals.amount = parsedArgument.getCrystals();
                acceptedClientValues = true;
            }

            if (acceptedClientValues) {
                playerSession.savePlayerSession();
            }

            if (playerSession.getPlayerSettings().getInventoryStorage().credits.amount != parsedArgument.getCredits()) {
                LOG.warn(action + " from Player " + parsedArgument.getPlayerId() + " credits is different to servers " +
                        playerSession.getPlayerSettings().getInventoryStorage().credits.amount + ", " +
                        parsedArgument.getCredits());
            }

            if (playerSession.getPlayerSettings().getInventoryStorage().materials.amount != parsedArgument.getMaterials()) {
                LOG.warn(action + " from Player " + parsedArgument.getPlayerId() + " materials is different to servers " +
                        playerSession.getPlayerSettings().getInventoryStorage().materials.amount + ", " +
                        parsedArgument.getMaterials());
            }

            if (playerSession.getPlayerSettings().getInventoryStorage().contraband.amount != parsedArgument.getContraband()) {
                LOG.warn(action + " from Player " + parsedArgument.getPlayerId() + " contraband is different to servers " +
                        playerSession.getPlayerSettings().getInventoryStorage().contraband.amount + ", " +
                        parsedArgument.getContraband());
            }

            if (playerSession.getPlayerSettings().getInventoryStorage().crystals.amount != parsedArgument.getCrystals()) {
                LOG.warn(action + " from Player " + parsedArgument.getPlayerId() + " crystals is different to servers " +
                        playerSession.getPlayerSettings().getInventoryStorage().crystals.amount + ", " +
                        parsedArgument.getCrystals());
            }
        }
    }

    protected boolean acceptCredits() {
        return false;
    }

    protected boolean acceptMaterials() {
        return false;
    }

    protected boolean acceptContraband() {
        return false;
    }

    protected boolean acceptCrystals() {
        return false;
    }

    protected boolean doResourceSyncCheck() {
        return true;
    }
}
