package swcnoops.server.model;

/**
 * Taken from game C#, will have to fix and test as we go along
 */
public enum SquadMsgType {
    Invalid,
    chat,
    join,
    joinRequest,
    joinRequestAccepted,
    joinRequestRejected,
    inviteAccepted,
    leave,
    ejected,
    promotion,
    demotion,
    shareBattle,
    troopRequest,
    troopDonation,
    warMatchMakingBegin,
    warMatchMakingCancel,
    warStarted,
    warPrepared,
    warBuffBaseAttackStart,
    warBuffBaseAttackComplete,
    warPlayerAttackStart,
    warPlayerAttackComplete,
    warEnded,
    squadLevelUp,
    perkUnlocked,
    perkUpgraded,
    perkInvest,
    invite,
    inviteRejected
}
