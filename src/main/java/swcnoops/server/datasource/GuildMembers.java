package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GuildMembers {
    final private String guildId;
    private List<Member> members = new ArrayList<>();
    private long lastUpdatedTime;
    private long dirtyTime;

    private Lock reloadLock = new ReentrantLock();

    public GuildMembers(String guildId, List<Member> members) {
        this.guildId = guildId;
        initialise(members);
        this.lastUpdatedTime = System.currentTimeMillis();
    }

    public String getGuildId() {
        return guildId;
    }

    private void initialise(List<Member> members) {
        this.members = this.padMembersToAllowWar(members);
    }

    private List<Member> padMembersToAllowWar(List<Member> members) {
        if (ServiceFactory.instance().getConfig().createBotPlayersInGroup) {
            int membersSize = members.size();
            if (membersSize < 15) {
                for (int i = 0; i < 15 - membersSize; i++) {
                    Member member = createDummyBot(this.getGuildId(), i);
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

    public void setDirty() {
        this.dirtyTime = System.currentTimeMillis();
    }

    public List<Member> getMembers() {
        List<Member> currentMap = getOrLoadMemberMap();
        return currentMap;
    }

    private List<Member> getOrLoadMemberMap() {
        List<Member> current = this.members;
        if (current == null || this.dirtyTime > this.lastUpdatedTime) {
            reloadLock.lock();
            try {
                if (this.members == null || this.dirtyTime > this.lastUpdatedTime) {
                    this.members = this.padMembersToAllowWar(reloadData());
                    this.lastUpdatedTime = System.currentTimeMillis();
                }
                current = this.members;
            } finally {
                reloadLock.unlock();
            }
        }

        return current;
    }

    private List<Member> reloadData() {
        List<Member> members = ServiceFactory.instance().getPlayerDatasource().loadSquadMembers(this.getGuildId());
        return members;
    }
}
