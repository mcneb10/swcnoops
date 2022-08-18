package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.response.GuildResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.SquadMemberWarData;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

public class GuildWarTroopsRequest extends GuildCommandAction<GuildWarTroopsRequest, GuildResult> {
    private boolean payToSkip;
    private String message;

    @Override
    protected GuildResult execute(GuildWarTroopsRequest arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        GuildResult guildResult = new GuildResult();
        GuildSession guildSession = playerSession.getGuildSession();
        if (guildSession != null) {
            SquadMemberWarData squadMemberWarData = playerSession.getSquadMemberWarData(time);
            SquadNotification squadNotification =
                    playerSession.troopsRequest(squadMemberWarData.donatedTroops, squadMemberWarData.warId,
                            arguments.isPayToSkip(), arguments.getMessage(), time);
            guildResult.setSquadNotification(squadNotification);
        }
        return guildResult;
    }

    @Override
    protected GuildWarTroopsRequest parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarTroopsRequest.class);
    }

    @Override
    public String getAction() {
        return "guild.war.troops.request";
    }

    public boolean isPayToSkip() {
        return payToSkip;
    }

    public String getMessage() {
        return message;
    }
}
