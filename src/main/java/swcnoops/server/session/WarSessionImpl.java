package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.PlayerBattleComplete;
import swcnoops.server.datasource.AttackDetail;
import swcnoops.server.datasource.DefendingWarParticipant;
import swcnoops.server.datasource.War;
import swcnoops.server.datasource.WarNotification;
import swcnoops.server.model.*;

import static swcnoops.server.session.NotificationFactory.createNotification;

public class WarSessionImpl implements WarSession {
    final private String warId;
    final private War war;
    private final GuildSession squadA;
    private final GuildSession squadB;

    public WarSessionImpl(String warId) {
        this.warId = warId;
        this.war = ServiceFactory.instance().getPlayerDatasource().getWar(warId);
        this.squadA = ServiceFactory.instance().getSessionManager().getGuildSession(this.war.getSquadIdA());
        this.squadB = ServiceFactory.instance().getSessionManager().getGuildSession(this.war.getSquadIdB());
    }

    @Override
    public String getWarId() {
        return warId;
    }

    @Override
    public String getGuildIdA() {
        return this.squadA.getGuildId();
    }

    @Override
    public String getGuildIdB() {
        return this.squadB.getGuildId();
    }

    @Override
    public GuildSession getGuildASession() {
        return squadA;
    }

    @Override
    public GuildSession getGuildBSession() {
        return squadB;
    }

    @Override
    public AttackDetail warAttackStart(PlayerSession playerSession, String opponentId, long time) {
        PlayerSession opponentSession = ServiceFactory.instance().getSessionManager().getPlayerSession(opponentId);
        WarNotificationData warNotificationData = new WarNotificationData(this.getWarId());
        warNotificationData.setOpponentId(opponentId);
        warNotificationData.setOpponentName(opponentSession.getPlayerSettings().getName());

        SquadNotification attackStartNotification =
                createNotification(playerSession.getGuildSession().getGuildId(),
                        playerSession.getGuildSession().getGuildName(), playerSession, SquadMsgType.warPlayerAttackStart);
        attackStartNotification.setData(warNotificationData);

        AttackDetail attackDetail = ServiceFactory.instance().getPlayerDatasource().warAttackStart(this,
                playerSession.getPlayerId(), opponentId, attackStartNotification, time);

        if (attackDetail.getBattleId() != null) {
            setGuildDirtyNotifcation(attackDetail);
        }

        return attackDetail;
    }

    @Override
    public void warMatched() {
        GuildSession guildSession1 = ServiceFactory.instance().getSessionManager().getGuildSession(this.getGuildIdA());
        SquadNotification warPreparedNotification =
                createNotification(guildSession1.getGuildId(), guildSession1.getGuildName(),
                        null, SquadMsgType.warPrepared);
        warPreparedNotification.setData(new WarNotificationData(warId));

        WarNotification warNotification = ServiceFactory.instance().getPlayerDatasource()
                .warPrepared(this, getWarId(), warPreparedNotification);

        this.squadA.getGuildSettings().setWarId(warId);
        this.squadB.getGuildSettings().setWarId(warId);
        setGuildDirtyNotifcation(warNotification);
    }

    private void setGuildDirtyNotifcation(WarNotification warNotification) {
        this.squadA.setNotificationDirty(warNotification.getGuildANotificationDate());
        this.squadB.setNotificationDirty(warNotification.getGuildBNotificationDate());
    }

    @Override
    public AttackDetail warAttackComplete(PlayerSession playerSession, PlayerBattleComplete playerBattleComplete, long time) {
        // we override the planet as this is war which is on sullust
        playerBattleComplete.getReplayData().combatEncounter.map.planet = "planet24";

        WarNotificationData warNotificationData = new WarNotificationData(this.getWarId());
        SquadNotification attackCompleteNotification =
                createNotification(playerSession.getGuildSession().getGuildId(),
                        playerSession.getGuildSession().getGuildName(), playerSession, SquadMsgType.warPlayerAttackComplete);
        attackCompleteNotification.setData(warNotificationData);

        DefendingWarParticipant defendingWarParticipant = ServiceFactory.instance().getPlayerDatasource()
                .getDefendingWarParticipantByBattleId(playerBattleComplete.getBattleId());

        PlayerSession opponentSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(defendingWarParticipant.getPlayerId());
        warNotificationData.setOpponentId(defendingWarParticipant.getPlayerId());
        warNotificationData.setOpponentName(opponentSession.getPlayerSettings().getName());

        SquadNotification attackReplayNotification =
                createWarReplayNotification(playerSession, opponentSession, playerBattleComplete);

        AttackDetail attackDetail = ServiceFactory.instance().getPlayerDatasource().warAttackComplete(this,
                playerSession.getPlayerId(), playerBattleComplete, attackCompleteNotification, attackReplayNotification,
                defendingWarParticipant,
                time);

        setGuildDirtyNotifcation(attackDetail);

        return attackDetail;
    }

    private SquadNotification createWarReplayNotification(PlayerSession playerSession,
                                                          PlayerSession opponentSession,
                                                          PlayerBattleComplete playerBattleComplete)
    {
        ShareBattleNotificationData shareBattleNotificationData =
                createBattleNotification(playerSession, opponentSession, playerBattleComplete);

        SquadNotification attackReplayNotification =
                createNotification(playerSession.getGuildSession().getGuildId(),
                        playerSession.getGuildSession().getGuildName(), playerSession, SquadMsgType.shareBattle);
        attackReplayNotification.setData(shareBattleNotificationData);
        attackReplayNotification.setMessage(playerSession.getPlayerSettings().getName() + " attacking " +
                opponentSession.getPlayerSettings().getName());

        return attackReplayNotification;
    }

    private ShareBattleNotificationData createBattleNotification(PlayerSession playerSession,
                                                                 PlayerSession opponentSession,
                                                                 PlayerBattleComplete playerBattleComplete)
    {
        ShareBattleNotificationData shareBattleNotificationData =
                new ShareBattleNotificationData(playerBattleComplete.getBattleId());

        shareBattleNotificationData.setBattleVersion(playerBattleComplete.getBattleVersion());
        shareBattleNotificationData.setCmsVersion(playerBattleComplete.getCmsVersion());
        shareBattleNotificationData.setBattleScoreDelta(0);
        shareBattleNotificationData.setFaction(playerSession.getFaction());
        shareBattleNotificationData.setStars(playerBattleComplete.getStars());
        shareBattleNotificationData.setDamagePercent(playerBattleComplete.getBaseDamagePercent());
        shareBattleNotificationData.setType(SquadBattleReplayType.Attack);
        shareBattleNotificationData.setOpponentId(opponentSession.getPlayerId());
        shareBattleNotificationData.setOpponentName(opponentSession.getPlayerSettings().getName());
        shareBattleNotificationData.setOpponentFaction(opponentSession.getFaction());

        return shareBattleNotificationData;
    }
}
