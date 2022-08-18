package swcnoops.server.commands;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.GuildMessage;
import swcnoops.server.model.SquadMessage;
import swcnoops.server.model.SquadMsgType;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.requests.*;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract public class AbstractCommandAction<A extends CommandArguments, R extends CommandResult> extends PlayerIdArguments
        implements CommandAction<R>
{
    @Override
    public ResponseData execute(Command command) throws Exception {
        JsonParser jsonParser = ServiceFactory.instance().getJsonParser();
        A parsedArgument = this.parseArgument(jsonParser, command.getArgs());
        command.setParsedArgument(parsedArgument);
        R commandResult = this.execute(parsedArgument, command.getTime());
        command.setResponse(commandResult);
        ResponseData responseData = createResponse(command, commandResult);
        return responseData;
    }

    @Override
    public R execute(Object args, long time) throws Exception {
        JsonParser jsonParser = ServiceFactory.instance().getJsonParser();
        A parsedArgument = this.parseArgument(jsonParser, args);
        R result = this.execute(parsedArgument, time);
        return result;
    }

    protected abstract R execute(A arguments, long time) throws Exception;

    protected abstract A parseArgument(JsonParser jsonParser, Object argumentObject);

    @Override
    public ResponseData createResponse(Command command, R commandResult) {
        ResponseData responseData = new ResponseData();
        responseData.requestId = command.getRequestId();

        if (commandResult != null) {
            responseData.result = commandResult.getResult();
            responseData.status = commandResult.getStatus();
        }

        responseData.messages = createMessage(command, commandResult);
        return responseData;
    }

    protected Messages createMessage(Command command, R commandResult) {
        PlayerSession playerSession = null;

        String playerId = null;

        if (command.getParsedArgument() != null && command.getParsedArgument().getPlayerId() != null) {
            playerId = command.getParsedArgument().getPlayerId();
        }

        if (playerId != null) {
            playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerId);
        }

        Messages messages;

        if (command.getAttachGuildNotification() &&
                playerSession != null && playerSession.hasNotificationsToSend())
        {
            messages = createGuildMessage(playerSession, command);
        } else {
            messages = new CommandMessages(command.getTime(), ServiceFactory.getSystemTimeSecondsFromEpoch(),
                    ServiceFactory.createRandomUUID());
        }

        return messages;
    }

    private Messages createGuildMessage(PlayerSession playerSession, Command command) {
        GuildSession guildSession = playerSession.getGuildSession();

        List<SquadNotification> allNotifications = new ArrayList<>();

        if (guildSession != null) {
            List<SquadNotification> squadNotifications = guildSession.getNotifications(playerSession.getNotificationsSince());
            if (squadNotifications != null)
                allNotifications.addAll(squadNotifications);
        }

        List<SquadNotification> playersNotifications = playerSession.getNotifications(playerSession.getNotificationsSince());

        if (playersNotifications != null) {
            List<SquadNotification> ejectedNotifications = playersNotifications.stream()
                    .filter(a -> a.getType() == SquadMsgType.ejected).collect(Collectors.toList());

            if (ejectedNotifications.size() > 0) {
                playerSession.removeEjectedNotifications(ejectedNotifications);
            }

            allNotifications.addAll(playersNotifications);
        }

        Messages messages = createMessage(command.getTime(), allNotifications);

        return messages;
    }

    private Messages createMessage(long firstCommandTime, List<SquadNotification> notifications) {
        String guid = ServiceFactory.createRandomUUID();
        long systemTime = ServiceFactory.getSystemTimeSecondsFromEpoch();
        GuildMessages messages = new GuildMessages(firstCommandTime, systemTime, guid);;

        if (notifications != null && notifications.size() > 0) {
            // only if there is a msg do we create notification
            for (SquadNotification squadNotification : notifications) {
                SquadMessage squadMessage = createSquadMessage(squadNotification.getGuildId(),
                        squadNotification.getGuildName(), squadNotification);
                GuildMessage guildMessage = new GuildMessage(squadMessage, guid, firstCommandTime);
                messages.getGuild().add(guildMessage);
            }
        }

        return messages;
    }

    static public SquadMessage createSquadMessage(String guildId, String guildName, SquadNotification squadNotification)
    {
        SquadMessage squadMessage = new SquadMessage(squadNotification);
        squadMessage.event = squadNotification.getType();
        squadMessage.guildId = guildId;
        squadMessage.guildName = guildName;
        squadMessage.level = 0;
        squadMessage.serverTime = squadNotification.getDate();
        return squadMessage;
    }

    static public <T extends CommandResult> T parseJsonFile(String filename, Class<T> clazz) {
        T response;

        try {
            response = ServiceFactory.instance().getJsonParser().toObjectFromResource(filename, clazz);
        } catch (Exception ex) {
            response = null;
        }

        return response;
    }

    @Override
    public boolean canAttachGuildNotifications() {
        return true;
    }
}
