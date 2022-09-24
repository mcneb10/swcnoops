package swcnoops.server.datasource;

import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

import java.util.List;

public interface GuildSettings {
    String getGuildId();

    List<Member> getMembers();

    boolean canSave();

    SquadNotification createTroopRequest(PlayerSession playerSession, String message);

    String troopDonationRecipient(PlayerSession playerSession, String recipientPlayerId);

    Squad getSquad();
}
