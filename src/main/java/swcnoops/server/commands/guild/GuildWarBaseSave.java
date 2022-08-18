package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Position;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

import java.util.Map;

public class GuildWarBaseSave extends AbstractCommandAction<GuildWarBaseSave, CommandResult> {
    private Map<String, Position> positions;

    @Override
    protected CommandResult execute(GuildWarBaseSave arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        playerSession.warBaseSave(arguments.getPositions(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected GuildWarBaseSave parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarBaseSave.class);
    }

    @Override
    public String getAction() {
        return "guild.war.base.save";
    }

    public Map<String, Position> getPositions() {
        return positions;
    }
}
