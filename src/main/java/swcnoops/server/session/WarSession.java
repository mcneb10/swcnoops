package swcnoops.server.session;

import swcnoops.server.datasource.AttackDetail;
import swcnoops.server.datasource.DefendingWarParticipant;
import swcnoops.server.model.BattleReplay;

import java.util.Map;

public interface WarSession {
    AttackDetail warAttackStart(PlayerSession playerSession, String opponentId, long time);

    String getGuildIdA();

    String getGuildIdB();

    GuildSession getGuildASession();

    GuildSession getGuildBSession();

    void warMatched();

    AttackDetail warAttackComplete(PlayerSession playerSession, PlayerSession defenderSession, BattleReplay arguments, Map<String, Integer> attackingUnitsKilled, DefendingWarParticipant defendingWarParticipant, long time);

    String getWarId();

    void processGuildGet(long time);

    void setDirty();
}
