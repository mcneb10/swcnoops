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

    public SelfDonatingSquad(PlayerSession playerSession) {
        this.faction = playerSession.getFaction();
        this.guildId = playerSession.getPlayerId();

        this.members.add(GuildHelper.createMember(playerSession));
        this.members.add(createDonateBot(playerSession.getPlayerSettings()));
    }

    private Member createDonateBot(PlayerSettings playerSettings) {
        Member member = new Member();
        member.isOfficer = false;
        member.isOwner = false;
        member.playerId = playerSettings.getPlayerId() + "-BOT";
        member.planet = playerSettings.getBaseMap().planet;
        member.joinDate = ServiceFactory.getSystemTimeSecondsFromEpoch();
        member.hqLevel = 5;
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
    public boolean getOpenEnrollment() {
        return false;
    }

    @Override
    public Integer getMinScoreAtEnrollment() {
        return Integer.valueOf(0);
    }

    @Override
    public Long getWarSignUpTime() {
        return null;
    }

    @Override
    public void setWarSignUpTime(Long warSignUpTime) {

    }

    @Override
    public void warMatchmakingStart(long time, List<String> participantIds) {

    }

    @Override
    public String getWarId() {
        return null;
    }

    @Override
    public void setWarId(String warId) {

    }

    @Override
    public void setDirty() {
    }

    @Override
    public List<WarHistory> getWarHistory() {
        return new ArrayList<>();
    }
}
