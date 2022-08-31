package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerDeployableCancel extends PlayerChecksum<PlayerDeployableCancel, CommandResult> {
    private String constructor;
    private String unitTypeId;
    private int quantity;

    @Override
    protected CommandResult execute(PlayerDeployableCancel arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.cancelTrainTroops(arguments.getConstructor(), arguments.getUnitTypeId(),
                arguments.getQuantity(),
                arguments.getCredits(),
                arguments.getContraband(),
                time);

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerDeployableCancel parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerDeployableCancel.class);
    }

    @Override
    public String getAction() {
        return "player.deployable.cancel";
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
