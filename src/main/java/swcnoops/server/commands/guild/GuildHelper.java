package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.Member;
import swcnoops.server.model.Perks;

public class GuildHelper {
    static public Member createMember(PlayerSettings playerSettings) {
        Member member = createMember(playerSettings.getPlayerId(), playerSettings.getName());
        member.isOfficer = false;
        member.isOwner = false;
        member.playerId = playerSettings.getPlayerId();
        member.planet = playerSettings.getBaseMap().planet;
        member.joinDate = ServiceFactory.getSystemTimeSecondsFromEpoch();
        member.hqLevel = 1;
        member.name = playerSettings.getName();
        return member;
    }

    static public Member createMember(String playerId, String playerName) {
        Member member = new Member();
        member.isOfficer = false;
        member.isOwner = false;
        member.playerId = playerId;
        member.joinDate = ServiceFactory.getSystemTimeSecondsFromEpoch();
        member.hqLevel = 1;
        member.name = playerName;
        return member;
    }

    static final public Perks emptyPerks = new Perks();

    public static Member createMember(String playerId, String playerName, boolean isOwner,
                                      boolean isOfficer, long joinDate, long troopsDonated, long troopsReceived,
                                      boolean warParty)
    {
        Member member = new Member();
        member.isOfficer = isOfficer;
        member.isOwner = isOwner;
        member.playerId = playerId;
        member.joinDate = joinDate;
        member.hqLevel = 1;
        member.name = playerName;
        member.troopsDonated = troopsDonated;
        member.troopsReceived = troopsReceived;
        member.warParty = warParty ? 1: 0;
        return member;
    }
}
