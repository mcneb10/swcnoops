package swcnoops.server.commands.guild;

import swcnoops.server.model.Member;
import swcnoops.server.model.Perks;
import swcnoops.server.session.PlayerSession;

public class GuildHelper {
    static final public Perks emptyPerks = new Perks();

    public static Member createMember(PlayerSession playerSession) {
        Member member = new Member();
        member.playerId = playerSession.getPlayerId();
        member.planet = playerSession.getPlayerSettings().getBaseMap().planet;
        member.name = playerSession.getPlayerSettings().getName();
        member.xp = playerSession.getScalarsManager().getObjectForReading().xp;
        member.hqLevel = playerSession.getHeadQuarter().getBuildingData().getLevel();
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
        member.hqLevel = hqLevel;
        member.name = playerName;
        member.troopsDonated = troopsDonated;
        member.troopsReceived = troopsReceived;
        member.warParty = warParty ? 1: 0;
        return member;
    }
}
