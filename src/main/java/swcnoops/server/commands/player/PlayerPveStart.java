package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPveStartCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.session.PlayerSession;

public class PlayerPveStart extends AbstractCommandAction<PlayerPveStart, PlayerPveStartCommandResult> {
    private String missionUid;
    private String battleUid;

    @Override
    protected PlayerPveStartCommandResult execute(PlayerPveStart arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.playerBattleStart(time);
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

    public String getMissionUid() {
        return missionUid;
    }

    public String getBattleUid() {
        return battleUid;
    }
}
