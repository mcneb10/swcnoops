package swcnoops.server.commands.player;

import com.fasterxml.jackson.annotation.JsonProperty;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerFueSetQuest extends AbstractCommandAction<PlayerFueSetQuest, CommandResult> {
    @JsonProperty("FueUid")
    private String FueUid;
    @Override
    protected CommandResult execute(PlayerFueSetQuest arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());
        playerSession.setCurrentQuest(arguments.getFueUid(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerFueSetQuest parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerFueSetQuest.class);
    }

    @Override
    public String getAction() {
        return "player.fue.setQuest";
    }

    public String getFueUid() {
        return FueUid;
    }
}
