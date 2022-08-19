package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.AttackDetail;
import swcnoops.server.datasource.War;
import swcnoops.server.datasource.WarNotification;
import swcnoops.server.model.SquadMsgType;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.model.WarNotificationData;

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
    public String warAttackStart(PlayerSession playerSession, String opponentId) {
        PlayerSession opponentSession = ServiceFactory.instance().getSessionManager().getPlayerSession(opponentId);
        WarNotificationData warNotificationData = new WarNotificationData(this.getWarId());
        warNotificationData.setOpponentId(opponentId);
        warNotificationData.setOpponentName(opponentSession.getPlayerSettings().getName());

        SquadNotification attackStartNotification =
                createNotification(playerSession.getGuildSession().getGuildId(),
                        playerSession.getGuildSession().getGuildName(), playerSession, SquadMsgType.warPlayerAttackStart);
        attackStartNotification.setData(warNotificationData);

        AttackDetail attackDetail = ServiceFactory.instance().getPlayerDatasource().warAttackStart(this, getWarId(),
                playerSession.getPlayerId(), opponentId, attackStartNotification);

        if (attackDetail.getBattleId() != null) {
            setGuildDirtyNotifcation(attackDetail);
        }

        return attackDetail.getBattleId();
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
}
