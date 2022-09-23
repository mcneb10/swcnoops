package swcnoops.server.session;

import swcnoops.server.model.BattleReplay;
import swcnoops.server.model.ShareBattleNotificationData;
import swcnoops.server.model.SquadBattleReplayType;

public class ShareBattleHelper {
    static public ShareBattleNotificationData createBattleNotification(PlayerSession playerSession,
                                                                 BattleReplay battleReplay)
    {
        ShareBattleNotificationData shareBattleNotificationData =
                new ShareBattleNotificationData(battleReplay.battleLog.battleId);

        shareBattleNotificationData.setBattleVersion(battleReplay.battleLog.battleVersion);
        shareBattleNotificationData.setCmsVersion(battleReplay.battleLog.cmsVersion);
        shareBattleNotificationData.setFaction(playerSession.getFaction());
        shareBattleNotificationData.setStars(battleReplay.battleLog.stars);
        shareBattleNotificationData.setDamagePercent(battleReplay.battleLog.baseDamagePercent);

        if (playerSession.getPlayerId().equals(battleReplay.battleLog.defender.playerId)) {
            shareBattleNotificationData.setType(SquadBattleReplayType.Defense);
            shareBattleNotificationData.setOpponentId(battleReplay.battleLog.attacker.playerId);
            shareBattleNotificationData.setOpponentName(battleReplay.battleLog.attacker.name);
            shareBattleNotificationData.setOpponentFaction(battleReplay.battleLog.attacker.faction);
            shareBattleNotificationData.setBattleScoreDelta(battleReplay.battleLog.defender.defenseRatingDelta);
        } else {
            shareBattleNotificationData.setType(SquadBattleReplayType.Attack);
            shareBattleNotificationData.setOpponentId(battleReplay.battleLog.defender.playerId);
            shareBattleNotificationData.setOpponentName(battleReplay.battleLog.defender.name);
            shareBattleNotificationData.setOpponentFaction(battleReplay.battleLog.defender.faction);
            shareBattleNotificationData.setBattleScoreDelta(battleReplay.battleLog.attacker.attackRatingDelta);
        }

        return shareBattleNotificationData;
    }
}
