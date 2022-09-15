package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.*;

public class NotificationFactory {
    final static public SquadNotification createNotification(String guildId, String guildName, PlayerSession playerSession,
                                                             SquadMsgType squadMsgType)
    {
        return createNotification(guildId, guildName, playerSession, null, squadMsgType);
    }

    final static public SquadNotification createNotification(String guildId, String guildName, PlayerSession playerSession,
                                                             String message, SquadMsgType squadMsgType)
    {
        SquadNotification squadNotification = null;
        PlayerSettings playerSettings = null;
        if (playerSession != null)
            playerSettings = playerSession.getPlayerSettings();

        if (squadMsgType != null) {
            switch (squadMsgType) {
                case leave:
                case join:
                case promotion:
                case demotion:
                case ejected:
                case joinRequest:
                case joinRequestAccepted:
                case joinRequestRejected:
                case warMatchMakingBegin:
                case warMatchMakingCancel:
                case warPrepared:
                case warPlayerAttackStart:
                case warPlayerAttackComplete:
                case shareBattle:
                    squadNotification =
                            new SquadNotification(guildId, guildName,
                                    ServiceFactory.createRandomUUID(), message,
                                    playerSettings != null ? playerSettings.getName() : null,
                                    playerSession != null ? playerSession.getPlayerId() : null, squadMsgType);
                    break;
                case warStarted:
                    squadNotification =
                            new SquadNotification(guildId, guildName,
                                    ServiceFactory.createRandomUUID(), message,
                                    null,
                                    null, squadMsgType);
                    break;
                default:
                    throw new RuntimeException("SquadMsgType not support yet " + squadMsgType);
            }
        }

        return squadNotification;
    }

    static public SquadNotificationData mapSquadNotificationData(SquadMsgType squadMessageType, String squadNotificationJson)
            throws Exception {
        SquadNotificationData squadNotificationData = null;
        if (squadMessageType != null) {
            switch (squadMessageType) {
                case troopRequest:
                    squadNotificationData = ServiceFactory.instance().getJsonParser()
                            .fromJsonString(squadNotificationJson, TroopRequestData.class);
                    break;
                case troopDonation:
                    squadNotificationData = ServiceFactory.instance().getJsonParser()
                            .fromJsonString(squadNotificationJson, TroopDonationData.class);
                    break;
                case warPlayerAttackComplete:
                case warPrepared:
                case warMatchMakingCancel:
                case warMatchMakingBegin:
                case warPlayerAttackStart:
                case warStarted:
                    squadNotificationData = ServiceFactory.instance().getJsonParser()
                            .fromJsonString(squadNotificationJson, WarNotificationData.class);
                    break;
                case demotion:
                case promotion:
                    squadNotificationData = ServiceFactory.instance().getJsonParser()
                            .fromJsonString(squadNotificationJson, SqmMemberData.class);
                    break;
                case joinRequestAccepted:
                    squadNotificationData = ServiceFactory.instance().getJsonParser()
                            .fromJsonString(squadNotificationJson, AcceptorSquadMemberApplyData.class);
                    break;
                case joinRequestRejected:
                    squadNotificationData = ServiceFactory.instance().getJsonParser()
                            .fromJsonString(squadNotificationJson, RejectorSquadMemberApplyData.class);
                    break;
                case shareBattle:
                    squadNotificationData = ServiceFactory.instance().getJsonParser()
                            .fromJsonString(squadNotificationJson, ShareBattleNotificationData.class);
                    break;
                case leave:
                case join:
                case ejected:
                case joinRequest:
                        break;
                default:
                    throw new RuntimeException("Unsupported squadMessageType " + squadMessageType);
            }
        }
        return squadNotificationData;
    }
}
