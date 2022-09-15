package swcnoops.server.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
        property = "notifType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AcceptorSquadMemberApplyData.class, name = "AcceptorSquadMember"),
        @JsonSubTypes.Type(value = RejectorSquadMemberApplyData.class, name = "RejectorSquadMember"),
        @JsonSubTypes.Type(value = ShareBattleNotificationData.class, name = "ShareBattle"),
        @JsonSubTypes.Type(value = SqmMemberData.class, name = "SqmMember"),
        @JsonSubTypes.Type(value = TroopDonationData.class, name = "TroopDonation"),
        @JsonSubTypes.Type(value = TroopRequestData.class, name = "TroopRequest"),
        @JsonSubTypes.Type(value = WarNotificationData.class, name = "WarNotification")})
public abstract class SquadNotificationData {
    private String notifType;

    public SquadNotificationData(String notifType) {
        this.notifType = notifType;
    }

    public String getNotifType() {
        return notifType;
    }

    public void setNotifType(String notifType) {
        this.notifType = notifType;
    }
}
