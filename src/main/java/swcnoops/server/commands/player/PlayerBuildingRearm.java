package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

import java.util.List;

public class PlayerBuildingRearm extends PlayerChecksum<PlayerBuildingRearm, CommandResult> {
    private List<String> buildingIds;

    @Override
    protected CommandResult execute(PlayerBuildingRearm arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());
        playerSession.rearm(arguments.getBuildingIds(),
                arguments.getCredits(),
                arguments.getMaterials(),
                arguments.getContraband(),
                time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerBuildingRearm parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBuildingRearm.class);
    }

    @Override
    public String getAction() {
        return "player.building.rearm";
    }

    public List<String> getBuildingIds() {
        return buildingIds;
    }
}
