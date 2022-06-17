package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerMissionsMissionMapCommandResult;
import swcnoops.server.json.JsonParser;

public class PlayerMissionsMissionMap extends AbstractCommandAction<PlayerMissionsMissionMap, PlayerMissionsMissionMapCommandResult>
{
    @Override
    protected PlayerMissionsMissionMapCommandResult execute(PlayerMissionsMissionMap arguments) throws Exception {
        PlayerMissionsMissionMapCommandResult playerMissionsMissionMapResponse = new PlayerMissionsMissionMapCommandResult();
        playerMissionsMissionMapResponse.setBattleId(ServiceFactory.createRandomUUID());
        return playerMissionsMissionMapResponse;
    }

    @Override
    protected PlayerMissionsMissionMap parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerMissionsMissionMap.class);
    }

    @Override
    public String getAction() {
        return "player.missions.missionMap";
    }
}
