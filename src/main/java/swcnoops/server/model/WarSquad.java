package swcnoops.server.model;

import swcnoops.server.session.GuildSession;

import java.util.ArrayList;
import java.util.List;

public class WarSquad {
    public String guildId;
    public String name;
    public FactionType faction;
    public List<Participant> participants = new ArrayList<>();

    static public WarSquad map(GuildSession guildSession) {
        WarSquad warSquad = new WarSquad();
        warSquad.guildId = guildSession.getGuildId();
        warSquad.name = guildSession.getGuildName();
        warSquad.faction = guildSession.getGuildSettings().getFaction();

        for (Member member : guildSession.getGuildSettings().getMembers()) {
            if (member.warParty != 0)
                warSquad.participants.add(map(member));
        }

        // a fudge for now to have 15 players on screen so that it works
        for (Member member : guildSession.getGuildSettings().getMembers()) {
            if (member.warParty == 0)
                warSquad.participants.add(map(member));
        }

        while (warSquad.participants.size() > 15)
            warSquad.participants.remove(15);

        return warSquad;
    }

    private static Participant map(Member member) {
        Participant participant = new Participant();
        participant.id = member.playerId;
        participant.name = member.name;
        participant.attacksWon = 0;
        participant.defensesWon = 0;
        participant.turns = 3;
        participant.score = 0;
        participant.victoryPoints = 3;
        participant.level = member.getHqLevel();
        return participant;
    }
}
