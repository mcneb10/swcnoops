package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

/**
 * This command builds troops, special attacks and repairs dekas.
 */
public class PlayerDeployableTrain extends PlayerChecksum<PlayerDeployableTrain, CommandResult> {
    private String constructor;
    private String unitTypeId;
    private int quantity;
    private String cs;

    @Override
    protected CommandResult execute(PlayerDeployableTrain arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.trainTroops(arguments.getConstructor(), arguments.getUnitTypeId(),
                arguments.getQuantity(), arguments.getCredits(), arguments.getContraband(), time);

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerDeployableTrain parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerDeployableTrain.class);
    }

    @Override
    public String getAction() {
        return "player.deployable.train";
    }

    public String getConstructor() {
        return constructor;
    }

    public String getUnitTypeId() {
        return unitTypeId;
    }

    public int getQuantity() {
        return quantity;
    }
}
