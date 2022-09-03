package swcnoops.server.session;

import swcnoops.server.datasource.AttackDetail;
import swcnoops.server.datasource.DefendingWarParticipant;
import swcnoops.server.model.BattleReplay;

public interface WarSession {
    AttackDetail warAttackStart(PlayerSession playerSession, String opponentId, long time);

    String getGuildIdA();

    String getGuildIdB();

    GuildSession getGuildASession();

    GuildSession getGuildBSession();

    void warMatched();

    AttackDetail warAttackComplete(PlayerSession playerSession, PlayerSession defenderSession, BattleReplay arguments, DefendingWarParticipant defendingWarParticipant, long time);

    String getWarId();

    void processGuildGet(long time);

    void setDirty();
}
