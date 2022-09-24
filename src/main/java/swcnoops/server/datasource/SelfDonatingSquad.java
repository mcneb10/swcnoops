package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.GuildHelper;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;

public class SelfDonatingSquad implements GuildSettings {
    static final private String NAME = "SelfDonateSquad";
    static final private String DonateBotName = "DonateBot";
    final private String guildId;
    final private List<Member> members = new ArrayList<>();
    final private Squad squad = new Squad();

    public SelfDonatingSquad(PlayerSession playerSession) {
        this.guildId = playerSession.getPlayerId();

        this.members.add(GuildHelper.createMember(playerSession));
        this.members.add(createDonateBot(playerSession));

        this.squad._id = this.getGuildId();
        this.squad.name = SelfDonatingSquad.NAME;
        this.squad.description = "SquadSymbols_11";
        this.squad.faction = playerSession.getFaction();
        this.squad.activeMemberCount = 1;
        this.squad.members = 1;
        this.squad.openEnrollment = true;
    }

    @Override
    public Squad getSquad() {
        return squad;
    }

    private Member createDonateBot(PlayerSession playerSession) {
        Member member = new Member();
        member.isOfficer = false;
        member.isOwner = false;
        member.playerId = playerSession.getPlayerId() + "-BOT";
        member.planet = playerSession.getPlayerSettings().getBaseMap().planet;
        member.joinDate = ServiceFactory.getSystemTimeSecondsFromEpoch();
        member.hqLevel = 5;
        member.name = SelfDonatingSquad.DonateBotName;
        return member;
    }

    @Override
    public String getGuildId() {
        return this.guildId;
    }

    @Override
    public List<Member> getMembers() {
        return members;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public SquadNotification createTroopRequest(PlayerSession playerSession, String message) {
        Member botMember = playerSession.getGuildSession().getMembersManager().getObjectForReading()
                .stream().filter(m -> m.name.equals(SelfDonatingSquad.DonateBotName)).findFirst().get();

        String playerId = botMember.playerId;
        String playerName = botMember.name;

        SquadNotification squadNotification = new SquadNotification(playerSession.getGuildSession().getGuildId(),
                playerSession.getGuildSession().getGuildName(),
                message, playerName, playerId, SquadMsgType.troopRequest);

        return squadNotification;
    }

    @Override
    public String troopDonationRecipient(PlayerSession playerSession, String recipientPlayerId) {
        return playerSession.getPlayerId();
    }
}
