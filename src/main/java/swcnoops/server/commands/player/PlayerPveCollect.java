package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerPveCollect extends PlayerChecksum<PlayerPveCollect, CommandResult> {
    private String missionUid;
    private String battleUid;

    @Override
    protected CommandResult execute(PlayerPveCollect arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.pveCollect(arguments.getMissionUid(), arguments.getBattleUid(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerPveCollect parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPveCollect.class);
    }

    @Override
    public String getAction() {
        return "player.pve.collect";
    }

    public String getMissionUid() {
        return missionUid;
    }

    public String getBattleUid() {
        return battleUid;
    }

    protected boolean acceptCredits() {
        return true;
    }

    protected boolean acceptMaterials() {
        return true;
    }

    protected boolean acceptContraband() {
        return true;
    }

    protected boolean acceptCrystals() {
        return true;
    }
}
