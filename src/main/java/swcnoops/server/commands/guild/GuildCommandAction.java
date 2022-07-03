package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.guild.response.GuildResult;
import swcnoops.server.model.GuildMessage;
import swcnoops.server.model.SquadMessage;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.requests.GuildMessages;
import swcnoops.server.requests.Messages;

abstract public class GuildCommandAction<A extends GuildCommandAction, B extends GuildResult>
        extends AbstractCommandAction<A, B>
{
    @Override
    protected Messages createMessage(Command command, GuildResult guildResult) {
        String guid = ServiceFactory.createRandomUUID();
        long systemTime = ServiceFactory.getSystemTimeSecondsFromEpoch();
        GuildMessages messages = new GuildMessages(command.getTime(), systemTime, guid);

        // only if there is a msg do we create notification
        if (guildResult.getSquadMsgType() != null) {
            SquadNotification squadNotification = createSquadNotification(systemTime, guid, guildResult);
            SquadMessage squadMessage = createSquadMessage(guildResult, squadNotification);
            GuildMessage guildMessage = new GuildMessage(squadMessage, guid, command.getTime());
            messages.getGuild().add(guildMessage);
        }

        return messages;
    }

    private SquadNotification createSquadNotification(long systemTime, String guid, GuildResult guildResult)
    {
        SquadNotification squadNotification =
                new SquadNotification(systemTime, guid,
                        guildResult.getSquadMessage(),
                        guildResult.getPlayerName(),
                        guildResult.getPlayerId(),
                        guildResult.getSquadMsgType(),
                        guildResult.getNotificationData());
        return squadNotification;
    }

    private SquadMessage createSquadMessage(GuildResult commandResult, SquadNotification squadNotification)
    {
        SquadMessage squadMessage = new SquadMessage(squadNotification);
        squadMessage.event = squadNotification.getType();
        squadMessage.guildId = commandResult.getGuildId();
        squadMessage.guildName = commandResult.getGuildName();
        squadMessage.level = 0;
        squadMessage.serverTime = squadNotification.getDate();
        return squadMessage;
    }
}
