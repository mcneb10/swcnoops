package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.response.GuildTroopsRequestCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

/**
 * For this to work, the notification sent out must have a playerId that is in the same squad.
 * As there is a check by client code for this condition.
 */
public class GuildTroopsRequest extends GuildCommandAction<GuildTroopsRequest, GuildTroopsRequestCommandResult> {
    private boolean payToSkip;
    private String message;

    @Override
    protected GuildTroopsRequestCommandResult execute(GuildTroopsRequest arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        SquadNotification squadNotification = playerSession.troopsRequest(arguments.isPayToSkip(), arguments.getMessage(), time);

        GuildTroopsRequestCommandResult guildTroopsRequestCommandResult =
                new GuildTroopsRequestCommandResult();

        guildTroopsRequestCommandResult.setSquadNotification(squadNotification);

        return guildTroopsRequestCommandResult;
    }

    @Override
    protected GuildTroopsRequest parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildTroopsRequest.class);
    }

    @Override
    public String getAction() {
        return "guild.troops.request";
    }

    public boolean isPayToSkip() {
        return payToSkip;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean canAttachGuildNotifications() {
        return false;
    }
}
