package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.GuildHelper;
import swcnoops.server.model.FactionType;
import swcnoops.server.model.Member;
import swcnoops.server.model.Perks;

import java.util.ArrayList;
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
}
