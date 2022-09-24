package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.guild.response.GuildResult;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.model.*;
import swcnoops.server.requests.GuildMessages;
import swcnoops.server.requests.Messages;
import swcnoops.server.session.GuildSession;

abstract public class GuildCommandAction<A extends GuildCommandAction, B extends GuildResult>
        extends AbstractCommandAction<A, B>
{
    @Override
    protected Messages createMessage(Command command, GuildResult guildResult) {
        String guid = ServiceFactory.createRandomUUID();
        long systemTime = ServiceFactory.getSystemTimeSecondsFromEpoch();
        GuildMessages messages = new GuildMessages(command.getTime(), systemTime, guid);

        // only if there is a msg do we create notification
        if (guildResult.getSquadNotification() != null) {
            SquadNotification squadNotification = guildResult.getSquadNotification();
            SquadMessage squadMessage = createSquadMessage(guildResult, squadNotification);
            GuildMessage guildMessage = new GuildMessage(squadMessage, guid, command.getTime());
            messages.getGuild().add(guildMessage);
        }

        return messages;
    }

    private SquadMessage createSquadMessage(GuildResult commandResult, SquadNotification squadNotification)
    {
        return AbstractCommandAction.createSquadMessage(commandResult.getGuildId(),
                commandResult.getGuildName(), squadNotification);
    }

    static protected SquadResult createSquadResult(GuildSession guildSession) {
        if (guildSession == null)
            return null;

        Squad squad = guildSession.getSquadManager().getObjectForReading();

        SquadResult squadResult = new SquadResult();
        squadResult.id = squad._id;
        squadResult.name = squad.name;
        squadResult.description = squad.description;
        squadResult.faction = squad.faction;
        squadResult.warHistory = guildSession.getWarHistoryManager().getObjectForReading();
        squadResult.currentWarId = squad.warId;
        squadResult.created = squad.created;
        squadResult.perks = new Perks();
        squadResult.members = guildSession.getMembersManager().getObjectForReading();
        squadResult.warSignUpTime = squad.warSignUpTime;
        squadResult.memberCount = squadResult.members.size();
        squadResult.activeMemberCount = squadResult.members.size();
        squadResult.icon = squad.icon;
        squadResult.rank = 1;
        squadResult.level = 1;
        squadResult.lastPerkNotif = 0;
        squadResult.score = 0;
        squadResult.squadWarReadyCount = 0;
        squadResult.membershipRestrictions = new MembershipRestrictions();
        squadResult.membershipRestrictions.faction = squad.faction;
        squadResult.membershipRestrictions.openEnrollment = squad.openEnrollment;
        squadResult.membershipRestrictions.maxSize = 20;
        squadResult.membershipRestrictions.minScoreAtEnrollment = squad.minScore;
        return squadResult;
    }
}
