package swcnoops.server.model;

public class SqmMemberData extends SquadNotificationData {
    public String memberId;
    public SquadRole toRank;

    public SqmMemberData() {
        super("SqmMember");
    }
}
