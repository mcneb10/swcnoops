package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

public class GuildWarMatchmakingCancel extends AbstractCommandAction<GuildWarMatchmakingCancel, CommandResult> {
    @Override
    protected CommandResult execute(GuildWarMatchmakingCancel arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = playerSession.getGuildSession();

        SquadNotification squadNotification = null;
        if (guildSession != null) {
            squadNotification = guildSession.warMatchmakingCancel(playerSession, time);
        }

        GuildResult guildResult = new GuildResult(guildSession);

        if (squadNotification == null) {
            guildResult.setErrorCode(ResponseHelper.STATUS_CODE_GUILD_WAR_WRONG_PHASE);
            guildResult.setSuccess(false);
        }

        guildResult.setSquadNotification(squadNotification);
        return guildResult;
    }

    @Override
    protected GuildWarMatchmakingCancel parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarMatchmakingCancel.class);
    }

    @Override
    public String getAction() {
        return "guild.war.matchmaking.cancel";
    }
}
