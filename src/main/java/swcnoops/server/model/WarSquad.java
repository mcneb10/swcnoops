package swcnoops.server.model;

import swcnoops.server.session.GuildSession;

import java.util.ArrayList;
import java.util.List;

public class WarSquad {
    public String guildId;
    public String name;
    public FactionType faction;
    public List<Participant> participants = new ArrayList<>();

    static public WarSquad map(GuildSession guildSession, List<SquadMemberWarData> participants) {
        WarSquad warSquad = new WarSquad();
        warSquad.guildId = guildSession.getGuildId();
        warSquad.name = guildSession.getGuildName();
        warSquad.faction = guildSession.getGuildSettings().getFaction();

        for (SquadMemberWarData squadMemberWarData : participants) {
            warSquad.participants.add(map(squadMemberWarData));
        }

        // a fudge for now to have 15 players on screen so that it works
//        for (Member member : guildSession.getGuildSettings().getMembers()) {
//            if (member.warParty == 0)
//                warSquad.participants.add(map(member));
//        }
//
//        while (warSquad.participants.size() > 15)
//            warSquad.participants.remove(15);

        return warSquad;
    }

    private static Participant map(SquadMemberWarData squadMemberWarData) {
        Participant participant = new Participant();
        participant.id = squadMemberWarData.id;
        participant.name = squadMemberWarData.name;
        participant.attacksWon = squadMemberWarData.attacksWon;
        participant.defensesWon = squadMemberWarData.defensesWon;
        participant.turns = squadMemberWarData.turns;
        participant.score = squadMemberWarData.score;
        participant.victoryPoints = squadMemberWarData.victoryPoints;
        participant.level = squadMemberWarData.level;

        if (squadMemberWarData.defenseExpirationDate != 0)
            participant.currentlyDefending.put("expiration", squadMemberWarData.defenseExpirationDate);
        return participant;
    }
}
