package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.ReadOnlyDBCacheObject;
import swcnoops.server.model.Member;

import java.util.ArrayList;
import java.util.List;

public class MembersManager extends ReadOnlyDBCacheObject<List<Member>> {
    final private String guildId;

    public MembersManager(String guildId, List<Member> initial) {
        super(false);
        this.guildId = guildId;
        initialise(initial);
    }

    @Override
    public void initialise(List<Member> initialDBObject) {
        super.initialise(padMembersToAllowWar(initialDBObject));
    }

    @Override
    protected List<Member> loadDBObject() {
        List<Member> members = ServiceFactory.instance().getPlayerDatasource().loadSquadMembers(this.guildId);
        return padMembersToAllowWar(members);
    }

    private List<Member> padMembersToAllowWar(List<Member> members) {
        if (members == null)
            members = new ArrayList<>();

        if (ServiceFactory.instance().getConfig().createBotPlayersInGroup && members.size() > 0) {
            int membersSize = members.size();
            if (membersSize < 15) {
                for (int i = 0; i < 15 - membersSize; i++) {
                    Member member = createDummyBot(this.guildId, i);
                    members.add(member);
                }
            }
        }
        return members;
    }

    private Member createDummyBot(String guildId, int botName) {
        Member member = new Member();
        member.isOfficer = false;
        member.isOwner = false;
        member.playerId = guildId + "-BOT" + botName;
        member.planet = "planet1";
        member.joinDate = ServiceFactory.getSystemTimeSecondsFromEpoch();
        member.hqLevel = 10;
        member.name = "BOT-" + botName;
        return member;
    }
}
