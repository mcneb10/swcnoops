package swcnoops.server.requests;

import swcnoops.server.commands.Command;
import swcnoops.server.commands.CommandAction;
import swcnoops.server.commands.CommandFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.model.GuildMessage;
import swcnoops.server.model.SquadMessage;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BatchProcessorImpl implements BatchProcessor {
    final private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'+01:00'");
    final private CommandFactory commandFactory = new CommandFactory();

    public BatchProcessorImpl() {
    }

    @Override
    public Batch decode(String batchRequestJson) throws Exception {
        Batch batch = ServiceFactory.instance().getJsonParser().fromJsonString(batchRequestJson, Batch.class);
        return batch;
    }

    @Override
    public void decodeCommands(Batch batch) {
        for (Command command : batch.getCommands()) {
            String action = command.getAction();
            CommandAction commandAction = this.commandFactory.get(action);

            if (commandAction != null) {
                command.setCommandAction(commandAction);
            } else {
                // TODO - not implemented yet what do we do for now
            }
        }
    }

    @Override
    public BatchResponse executeCommands(Batch batch) throws Exception {
        List<ResponseData> responseDatums = new ArrayList<>(batch.getCommands().size());

        String playerId = null;
        long firstCommandTime = 0;

        for (Command command : batch.getCommands()) {
            CommandAction commandAction = command.getCommandAction();

            if (commandAction == null) {
                throw new Exception("Command " + command.getAction() + " not supported");
            }

            CommandResult commandResult = commandAction.execute(command.getArgs(), command.getTime());
            command.setResponse(commandResult);
            ResponseData responseData = commandAction.createResponse(command, commandResult);
            responseDatums.add(responseData);

            if (playerId == null && commandResult.getRequestPlayerId() != null) {
                playerId = commandResult.getRequestPlayerId();
                firstCommandTime = command.getTime();
            }
        }

        // attach guild notifications
        if (responseDatums.size() > 0 && playerId != null) {
            ResponseData firstResponseData = responseDatums.get(0);
            if (firstResponseData.status == Integer.valueOf(0)) {
                boolean canReplace = true;
                if (firstResponseData.messages instanceof GuildMessages) {
                    GuildMessages guildMessages = (GuildMessages) firstResponseData.messages;
                    if (guildMessages.getGuild().size() > 0) {
                        canReplace = false;
                    }
                }

                if (canReplace) {
                    PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerId);
                    if (playerSession.getGuildSession() != null && playerSession.canSendNotifications()) {
                        List<SquadNotification> squadNotifications =
                                playerSession.getGuildSession().getNotificationsSince(playerSession.getNotificationsSince());
                        GuildSession guildSession = playerSession.getGuildSession();
                        Messages messages = createMessage(firstCommandTime, guildSession.getGuildId(),
                                guildSession.getGuildName(), squadNotifications);
                        if (messages != null)
                            firstResponseData.messages = messages;
                    }
                }
            }
        }

        BatchResponse batchResponse = new BatchResponse(responseDatums);
        batchResponse.setProtocolVersion(ServiceFactory.instance().getConfig().PROTOCOL_VERSION);
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        batchResponse.setServerTimestamp(zonedDateTime.toEpochSecond());
        batchResponse.setServerTime(dateTimeFormatter.format(zonedDateTime));
        return batchResponse;
    }

    private Messages createMessage(long firstCommandTime, String guildId, String guildName, List<SquadNotification> notifications) {
        GuildMessages messages = null;

        if (notifications != null && notifications.size() > 0) {
            String guid = ServiceFactory.createRandomUUID();
            long systemTime = ServiceFactory.getSystemTimeSecondsFromEpoch();
            messages = new GuildMessages(firstCommandTime, systemTime, guid);

            // only if there is a msg do we create notification
            for (SquadNotification squadNotification : notifications) {
                SquadMessage squadMessage = createSquadMessage(guildId, guildName, squadNotification);
                GuildMessage guildMessage = new GuildMessage(squadMessage, guid, firstCommandTime);
                messages.getGuild().add(guildMessage);
            }
        }

        return messages;
    }

    private SquadMessage createSquadMessage(String guildId, String guildName, SquadNotification squadNotification)
    {
        SquadMessage squadMessage = new SquadMessage(squadNotification);
        squadMessage.event = squadNotification.getType();
        squadMessage.guildId = guildId;
        squadMessage.guildName = guildName;
        squadMessage.level = 0;
        squadMessage.serverTime = squadNotification.getDate();
        return squadMessage;
    }

    @Override
    public String processBatchPostBody(String batchJson) throws Exception {
        Batch batch = this.decode(batchJson);
        this.decodeCommands(batch);
        BatchResponse batchResponse = this.executeCommands(batch);
        String json = ServiceFactory.instance().getJsonParser().toJson(batchResponse);
        return json;
    }

    public static Map<String, List<String>> decodeParameters(String queryString) {
        Map<String, List<String>> parms = new HashMap<String, List<String>>();
        if (queryString != null) {
            StringTokenizer st = new StringTokenizer(queryString, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf('=');
                String propertyName = sep >= 0 ? decodePercent(e.substring(0, sep)).trim() : decodePercent(e).trim();
                if (!parms.containsKey(propertyName)) {
                    parms.put(propertyName, new ArrayList<String>());
                }
                String propertyValue = sep >= 0 ? decodePercent(e.substring(sep + 1)) : null;
                if (propertyValue != null) {
                    parms.get(propertyName).add(propertyValue);
                }
            }
        }
        return parms;
    }

    public static String decodePercent(String str) {
        String decoded = null;
        try {
            decoded = URLDecoder.decode(str, "UTF8");
        } catch (UnsupportedEncodingException ignored) {
            throw new RuntimeException(ignored);
        }
        return decoded;
    }
}
