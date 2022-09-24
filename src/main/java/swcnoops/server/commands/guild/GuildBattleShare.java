package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.SessionManager;

public class GuildBattleShare extends AbstractCommandAction<GuildBattleShare, CommandResult> {
    private String battleId;
    private String message;

    @Override
    protected CommandResult execute(GuildBattleShare arguments, long time) throws Exception {
        SessionManager sessionManager = ServiceFactory.instance().getSessionManager();
        PlayerSession playerSession = sessionManager.getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = playerSession.getGuildSession();
        int retCode = guildSession.battleShare(playerSession, arguments.getBattleId(), arguments.getMessage());
        return ResponseHelper.newStatusResult(retCode);
    }

    @Override
    protected GuildBattleShare parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildBattleShare.class);
    }

    @Override
    public String getAction() {
        return "guild.battle.share";
    }

    public String getBattleId() {
        return battleId;
    }

    public String getMessage() {
        return message;
    }
}
