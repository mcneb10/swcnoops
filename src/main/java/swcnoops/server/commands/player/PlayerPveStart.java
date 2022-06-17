package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPveStartCommandResult;
import swcnoops.server.json.JsonParser;

public class PlayerPveStart extends AbstractCommandAction<PlayerPveStart, PlayerPveStartCommandResult> {
    @Override
    protected PlayerPveStartCommandResult execute(PlayerPveStart arguments) throws Exception {
        PlayerPveStartCommandResult playerPveStartResponse = new PlayerPveStartCommandResult();
        playerPveStartResponse.setBattleId(ServiceFactory.createRandomUUID());
        return playerPveStartResponse;
    }

    @Override
    protected PlayerPveStart parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPveStart.class);
    }

    @Override
    public String getAction() {
        return "player.pve.start";
    }
}
