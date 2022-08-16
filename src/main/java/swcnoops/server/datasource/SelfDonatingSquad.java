package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.GuildHelper;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SelfDonatingSquad implements GuildSettings {
    static final public String NAME = "SelfDonateSquad";
    static final public String DonateBotName = "DonateBot";
    final private FactionType faction;
    final private String guildId;
    final private List<Member> members = new ArrayList<>();

    public SelfDonatingSquad(PlayerSettings playerSettings) {
        this.faction = playerSettings.getFaction();
        this.guildId = playerSettings.getPlayerId();

        this.members.add(GuildHelper.createMember(playerSettings));
        this.members.add(createDonateBot(playerSettings));
    }

    private Member createDonateBot(PlayerSettings playerSettings) {
        Member member = new Member();
        member.isOfficer = false;
        member.isOwner = false;
        member.playerId = playerSettings.getPlayerId() + "-BOT";
        member.planet = playerSettings.getBaseMap().planet;
        member.joinDate = ServiceFactory.getSystemTimeSecondsFromEpoch();
        member.hqLevel = 1;
        member.name = DonateBotName;
        return member;
    }

    @Override
    public String getGuildId() {
        return this.guildId;
    }

    @Override
    public String getGuildName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Allows self donations to SC";
    }

    @Override
    public FactionType getFaction() {
        return faction;
    }

    @Override
    public long getCreated() {
        return 0;
    }

    @Override
    public Perks getPerks() {
        return GuildHelper.emptyPerks;
    }

    @Override
    public List<Member> getMembers() {
        return members;
    }

    @Override
    public String getIcon() {
        return "SquadSymbols_11";
    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public void setIcon(String icon) {

    }

    @Override
    public void setMinScoreAtEnrollment(Integer minScoreAtEnrollment) {

    }

    @Override
    public void setOpenEnrollment(boolean openEnrollment) {

    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public void addMember(String playerId, String playerName, boolean isOwner, boolean isOfficer, long jointDate, long troopsDonated, long troopsReceived) {
    }

    @Override
    public void removeMember(String playerId) {
    }

    @Override
    public SquadNotification createTroopRequest(PlayerSession playerSession, String message) {
        Member botMember = playerSession.getGuildSession().getGuildSettings().getMembers()
                .stream().filter(m -> m.name.equals(SelfDonatingSquad.DonateBotName)).findFirst().get();

        String playerId = botMember.playerId;
        String playerName = botMember.name;

        SquadNotification squadNotification = new SquadNotification(playerSession.getGuildSession().getGuildId(),
                playerSession.getGuildSession().getGuildName(),
                ServiceFactory.createRandomUUID(), message, playerName, playerId, SquadMsgType.troopRequest);

        return squadNotification;
    }

    @Override
    public String troopDonationRecipient(PlayerSession playerSession, String recipientPlayerId) {
        return playerSession.getPlayerId();
    }

    @Override
    public void addSquadNotification(SquadNotification squadNotification) {

    }

    @Override
    public Collection<? extends SquadNotification> getSquadNotifications() {
        return null;
    }

    @Override
    public Member getMember(String playerId) {
        return null;
    }

    @Override
    public boolean getOpenEnrollment() {
        return false;
    }

    @Override
    public Integer getMinScoreAtEnrollment() {
        return null;
    }
}
