package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.response.GuildResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

import java.util.List;

public class GuildWarMatchmakingStart extends GuildCommandAction<GuildWarMatchmakingStart, GuildResult> {
    private List<String> participantIds;
    private boolean isSameFactionWarAllowed;

    @Override
    protected GuildResult execute(GuildWarMatchmakingStart arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = playerSession.getGuildSession();

        SquadNotification squadNotification = null;
        if (guildSession != null) {
            squadNotification = guildSession.warMatchmakingStart(playerSession, arguments.getParticipantIds(),
                    arguments.isSameFactionWarAllowed, time);
        }

        GuildResult guildResult = new GuildResult(guildSession);
        guildResult.setSquadNotification(squadNotification);
        if (squadNotification == null) {
            guildResult.setErrorCode(ResponseHelper.STATUS_CODE_GUILD_WAR_WRONG_PHASE);
            guildResult.setSuccess(false);
        }

        return guildResult;
    }

    @Override
    protected GuildWarMatchmakingStart parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarMatchmakingStart.class);
    }

    @Override
    public String getAction() {
        return "guild.war.matchmaking.start";
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public boolean isSameFactionWarAllowed() {
        return isSameFactionWarAllowed;
    }
}
