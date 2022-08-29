package swcnoops.server.session;

import swcnoops.server.commands.player.PlayerBattleComplete;
import swcnoops.server.datasource.AttackDetail;

public interface WarSession {
    AttackDetail warAttackStart(PlayerSession playerSession, String opponentId, long time);

    String getGuildIdA();

    String getGuildIdB();

    GuildSession getGuildASession();

    GuildSession getGuildBSession();

    void warMatched();

    AttackDetail warAttackComplete(PlayerSession playerSession, PlayerBattleComplete arguments, long time);

    String getWarId();
}
