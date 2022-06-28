package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.guild.response.GuildTroopsRequestCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.requests.*;
import swcnoops.server.session.PlayerSession;

/**
 * For this to work, the notification sent out must have a playerId that is in the same squad.
 * As there is a check by client code for this condition.
 */
public class GuildTroopsRequest extends AbstractCommandAction<GuildTroopsRequest, GuildTroopsRequestCommandResult> {
    private boolean payToSkip;
    private String message;

    @Override
    protected GuildTroopsRequestCommandResult execute(GuildTroopsRequest arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.troopsRequest(arguments.isPayToSkip(), arguments.getMessage(), time);
        TroopRequestData troopRequestData = new TroopRequestData();
        troopRequestData.totalCapacity = 42;
        troopRequestData.troopDonationLimit = troopRequestData.totalCapacity;
        troopRequestData.amount = 42;

        GuildTroopsRequestCommandResult guildTroopsRequestCommandResult =
                new GuildTroopsRequestCommandResult(arguments.getMessage(),
                        "2c2d4aea-7f38-11e5-a29f-069096004f6a",
                        "SelfDonateBot",
                        troopRequestData,
                        playerSession.getGuildSession());
        return guildTroopsRequestCommandResult;
    }

    @Override
    protected Messages createMessage(Command command, GuildTroopsRequestCommandResult guildTroopsRequestCommandResult) {
        String guid = ServiceFactory.createRandomUUID();
        long systemTime = ServiceFactory.getSystemTimeSecondsFromEpoch();

        SquadNotification squadNotification =
                createSquadNotification(systemTime,guid, guildTroopsRequestCommandResult);
        SquadMessage squadMessage = createSquadMessage(systemTime, guildTroopsRequestCommandResult, squadNotification);
        GuildMessage guildMessage = new GuildMessage(squadMessage, guid, command.getTime());
        GuildMessages messages = new GuildMessages(command.getTime(), systemTime, guid);
        messages.getGuild().add(guildMessage);
        return messages;
    }

    private SquadNotification createSquadNotification(long systemTime, String guid,
                                                      GuildTroopsRequestCommandResult guildTroopsRequestCommandResult)
    {
        SquadNotification squadNotification =
                new SquadNotification(systemTime, guid,
                        guildTroopsRequestCommandResult.getMessage(),
                        guildTroopsRequestCommandResult.getName(),
                        guildTroopsRequestCommandResult.getPlayerId(),
                        SquadMsgType.troopRequest,
                        guildTroopsRequestCommandResult.getTroopRequestData());
        return squadNotification;
    }

    private SquadMessage createSquadMessage(long systemTime, GuildTroopsRequestCommandResult guildTroopsRequestCommandResult,
                                            SquadNotification squadNotification)
    {
        SquadMessage squadMessage = new SquadMessage(squadNotification);
        squadMessage.event = SquadMsgType.troopRequest;
        squadMessage.guildId = guildTroopsRequestCommandResult.getGuildSession().getGuildId();
        squadMessage.guildName = guildTroopsRequestCommandResult.getGuildSession().getGuildName();
        squadMessage.level = 0;
        squadMessage.serverTime = systemTime;
        return squadMessage;
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
}
