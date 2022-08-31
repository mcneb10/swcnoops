package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerBuildingCollect extends PlayerChecksum<PlayerBuildingCollect, CommandResult> {
    private String buildingId;

    @Override
    protected CommandResult execute(PlayerBuildingCollect arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.buildingCollect(arguments.getBuildingId(), arguments.getCredits(),
                arguments.getMaterials(), arguments.getContraband(), arguments.getCrystals(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerBuildingCollect parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBuildingCollect.class);
    }

    @Override
    public String getAction() {
        return "player.building.collect";
    }

    public String getBuildingId() {
        return buildingId;
    }
}
