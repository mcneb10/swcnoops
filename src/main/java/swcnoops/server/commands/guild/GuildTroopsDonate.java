package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.guild.response.GuildTroopsDonateCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.requests.GuildMessages;
import swcnoops.server.requests.Messages;
import swcnoops.server.session.PlayerSession;

import java.util.Map;

/**
 * TODO - finish this properly, for now it is self donating
 */
public class GuildTroopsDonate extends AbstractCommandAction<GuildTroopsDonate, GuildTroopsDonateCommandResult> {
    private Map<String, Integer> troopsDonated;
    private String recipientId;
    private String requestId;

    @Override
    protected GuildTroopsDonateCommandResult execute(GuildTroopsDonate arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        // TODO - when we do it for real, we use the real recipientId
        String recipientPlayerId = arguments.getPlayerId();

        playerSession.getGuildSession().processDonations(arguments.getTroopsDonated(), arguments.getRequestId(),
                playerSession, recipientPlayerId, time);


        // TODO - do we need to deal with any race conditions when multiple players
        // are trying to donate to the same player, a problem to be solved when doing squad support

        // not sure about this yet, this probably controls the SC space that the client has on screen
        TroopDonationProgress troopDonationProgress = null;
        GuildTroopsDonateCommandResult guildTroopsDonateCommandResult =
                new GuildTroopsDonateCommandResult(arguments.getTroopsDonated(),
                        false, troopDonationProgress);

        guildTroopsDonateCommandResult.setPlayerId(recipientPlayerId);
        guildTroopsDonateCommandResult.setName(playerSession.getPlayer().getPlayerSettings().getName());
        guildTroopsDonateCommandResult.setRequestId(requestId);

        TroopDonationData troopDonationData = new TroopDonationData();
        troopDonationData.troopsDonated = arguments.getTroopsDonated();
        troopDonationData.amount = arguments.getTroopsDonated().size();
        troopDonationData.requestId = arguments.getRequestId();
        troopDonationData.recipientId = recipientPlayerId;

        guildTroopsDonateCommandResult.setTroopDonationData(troopDonationData);
        guildTroopsDonateCommandResult.setGuildSession(playerSession.getGuildSession());

        return guildTroopsDonateCommandResult;
    }

    @Override
    protected Messages createMessage(Command command, GuildTroopsDonateCommandResult guildTroopsDonateCommandResult) {
        String guid = ServiceFactory.createRandomUUID();
        long systemTime = ServiceFactory.getSystemTimeSecondsFromEpoch();

        SquadNotification squadNotification =
                createSquadNotification(systemTime,guid, guildTroopsDonateCommandResult);
        SquadMessage squadMessage = createSquadMessage(systemTime, guildTroopsDonateCommandResult, squadNotification);
        GuildMessage guildMessage = new GuildMessage(squadMessage, guid, command.getTime());
        GuildMessages messages = new GuildMessages(command.getTime(), systemTime, guid);
        messages.getGuild().add(guildMessage);
        return messages;
    }

    private SquadNotification createSquadNotification(long systemTime, String guid,
                                                      GuildTroopsDonateCommandResult guildTroopsDonateCommandResult)
    {
        SquadNotification squadNotification =
                new SquadNotification(systemTime, guid,
                        "Donated by someone",
                        guildTroopsDonateCommandResult.getName(),
                        guildTroopsDonateCommandResult.getPlayerId(),
                        SquadMsgType.troopDonation,
                        guildTroopsDonateCommandResult.getTroopDonationData());
        return squadNotification;
    }

    private SquadMessage createSquadMessage(long systemTime, GuildTroopsDonateCommandResult guildTroopsDonateCommandResult,
                                            SquadNotification squadNotification)
    {
        SquadMessage squadMessage = new SquadMessage(squadNotification);
        squadMessage.event = SquadMsgType.troopRequest;
        squadMessage.guildId = guildTroopsDonateCommandResult.getGuildSession().getGuildId();
        squadMessage.guildName = guildTroopsDonateCommandResult.getGuildSession().getGuildName();
        squadMessage.level = 0;
        squadMessage.serverTime = systemTime;
        return squadMessage;
    }

    @Override
    protected GuildTroopsDonate parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildTroopsDonate.class);
    }

    @Override
    public String getAction() {
        return "guild.troops.donate";
    }

    public Map<String, Integer> getTroopsDonated() {
        return troopsDonated;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getRequestId() {
        return requestId;
    }
}
