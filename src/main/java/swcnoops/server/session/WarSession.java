package swcnoops.server.session;

public interface WarSession {
    String warAttackStart(PlayerSession playerSession, String opponentId);

    String getGuildIdA();

    String getGuildIdB();

    GuildSession getGuildASession();

    GuildSession getGuildBSession();

    void warMatched();
}
