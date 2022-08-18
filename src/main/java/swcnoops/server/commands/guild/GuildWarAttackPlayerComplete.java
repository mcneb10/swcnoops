package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.PlayerBattleComplete;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

public class GuildWarAttackPlayerComplete extends PlayerBattleComplete<GuildWarAttackPlayerComplete, CommandResult> {
    @Override
    protected CommandResult execute(GuildWarAttackPlayerComplete arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        // TODO - save and send out notification
        GuildSession guildSession = playerSession.getGuildSession();
        guildSession.warAttackComplete(arguments, playerSession);

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected GuildWarAttackPlayerComplete parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarAttackPlayerComplete.class);
    }

    @Override
    public String getAction() {
        return "guild.war.attackPlayer.complete";
    }
}
