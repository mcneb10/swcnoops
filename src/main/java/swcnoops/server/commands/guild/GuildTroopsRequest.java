package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.guild.response.GuildTroopsCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.requests.*;
import swcnoops.server.session.PlayerSession;

public class GuildTroopsRequest extends AbstractCommandAction<GuildTroopsRequest, GuildTroopsCommandResult> {
    private boolean payToSkip;
    private String message;

    @Override
    protected GuildTroopsCommandResult execute(GuildTroopsRequest arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.troopsRequest(arguments.isPayToSkip(), arguments.getMessage(), time);
        TroopRequestData troopRequestData = new TroopRequestData();
        troopRequestData.totalCapacity = 42;
        troopRequestData.troopDonationLimit = troopRequestData.totalCapacity;
        troopRequestData.amount = 42;

        GuildTroopsCommandResult guildTroopsCommandResult =
                new GuildTroopsCommandResult(arguments.getMessage(),
                        "2c2d4aea-7f38-11e5-a29f-069096004f6a",
                        "SelfDonateBot",
                        troopRequestData,
                        playerSession.getGuildSession());
        return guildTroopsCommandResult;
    }

    @Override
    protected Messages createMessage(Command command, GuildTroopsCommandResult guildTroopsCommandResult) {
        String guid = ServiceFactory.createRandomUUID();
        long systemTime = ServiceFactory.getSystemTimeSecondsFromEpoch();

        SquadNotification squadNotification =
                createSquadNotification(systemTime,guid,guildTroopsCommandResult);
        SquadMessage squadMessage = createSquadMessage(systemTime, guildTroopsCommandResult, squadNotification);
        GuildMessage guildMessage = new GuildMessage(squadMessage, guid, command.getTime());
        GuildMessages messages = new GuildMessages(command.getTime(), systemTime, guid);
        messages.getGuild().add(guildMessage);
        return messages;
    }

    private SquadNotification createSquadNotification(long systemTime, String guid,
                                                      GuildTroopsCommandResult guildTroopsCommandResult)
    {
        SquadNotification squadNotification =
                new SquadNotification(systemTime, guid,
                        guildTroopsCommandResult.getMessage(),
                        guildTroopsCommandResult.getName(),
                        guildTroopsCommandResult.getPlayerId(),
                        SquadMsgType.troopRequest,
                        guildTroopsCommandResult.getTroopRequestData());
        return squadNotification;
    }

    private SquadMessage createSquadMessage(long systemTime, GuildTroopsCommandResult guildTroopsCommandResult,
                                            SquadNotification squadNotification)
    {
        SquadMessage squadMessage = new SquadMessage(squadNotification);
        squadMessage.event = SquadMsgType.troopRequest;
        squadMessage.guildId = guildTroopsCommandResult.getGuildSession().getGuildId();
        squadMessage.guildName = guildTroopsCommandResult.getGuildSession().getGuildName();
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
