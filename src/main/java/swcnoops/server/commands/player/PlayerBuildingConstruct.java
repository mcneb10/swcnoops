package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Position;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerBuildingConstruct extends PlayerChecksum<PlayerBuildingConstruct, CommandResult> {
    private String buildingUid;
    private boolean payWithHardCurrency;
    private Position position;
    private String tag;

    @Override
    protected CommandResult execute(PlayerBuildingConstruct arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.buildingConstruct(arguments.getBuildingUid(),
                arguments.getTag(),
                arguments.getPosition(),
                arguments.getCredits(),
                arguments.getMaterials(),
                arguments.getContraband(),
                time);

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerBuildingConstruct parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBuildingConstruct.class);
    }

    @Override
    public String getAction() {
        return "player.building.construct";
    }

    public String getBuildingUid() {
        return buildingUid;
    }

    public boolean isPayWithHardCurrency() {
        return payWithHardCurrency;
    }

    public Position getPosition() {
        return position;
    }

    public String getTag() {
        return tag;
    }
}
