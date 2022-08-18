package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.Member;
import swcnoops.server.model.Perks;
import swcnoops.server.session.PlayerSession;

public class GuildHelper {
    static final public Perks emptyPerks = new Perks();

    public static Member createMember(PlayerSession playerSession) {
        Member member = new Member(playerSession);
        return member;
    }

    public static Member createMember(String playerId, String playerName, boolean isOwner,
                                      boolean isOfficer, long joinDate, long troopsDonated, long troopsReceived,
                                      boolean warParty, int hqLevel)
    {
        Member member = new Member();
        member.isOfficer = isOfficer;
        member.isOwner = isOwner;
        member.playerId = playerId;
        member.joinDate = joinDate;
        member.setLevel(hqLevel);
        member.name = playerName;
        member.troopsDonated = troopsDonated;
        member.troopsReceived = troopsReceived;
        member.warParty = warParty ? 1: 0;
        return member;
    }
}
