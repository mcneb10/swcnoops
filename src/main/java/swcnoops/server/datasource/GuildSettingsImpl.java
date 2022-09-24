package swcnoops.server.datasource;

import swcnoops.server.model.*;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import java.util.List;

public class GuildSettingsImpl implements GuildSettings {
    private String id;

    private List<Member> members;
    private Squad squad;

    public GuildSettingsImpl(String id) {
        this.id = id;
    }

    @Override
    public List<Member> getMembers() {
        return this.members;
    }

    @Override
    public String getGuildId() {
        return id;
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public SquadNotification createTroopRequest(PlayerSession playerSession, String message) {
        String playerId = playerSession.getPlayerId();
        String playerName = playerSession.getPlayerSettings().getName();

        GuildSession guildSession = playerSession.getGuildSession();
        SquadNotification squadNotification = null;

        if (guildSession != null) {
            squadNotification = new SquadNotification(playerSession.getGuildSession().getGuildId(),
                    playerSession.getGuildSession().getGuildName(),
                    message, playerName, playerId, SquadMsgType.troopRequest);
        }

        return squadNotification;
    }

    @Override
    public String troopDonationRecipient(PlayerSession playerSession, String recipientPlayerId) {
        return recipientPlayerId;
    }

    public void setMembers(List<Member> squadMembers) {
        this.members = squadMembers;
    }

    public void setSquad(Squad squad) {
        this.squad = squad;
    }

    public Squad getSquad() {
        return squad;
    }

    @Override
    public boolean canPadMembers() {
        return true;
    }

    @Override
    public boolean canReload() {
        return true;
    }
}
