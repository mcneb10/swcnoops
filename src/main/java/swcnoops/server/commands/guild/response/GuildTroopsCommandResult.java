package swcnoops.server.commands.guild.response;

import swcnoops.server.model.TroopRequestData;
import swcnoops.server.requests.AbstractCommandResult;
import swcnoops.server.session.GuildSession;

public class GuildTroopsCommandResult extends AbstractCommandResult {
    final private String message;
    final private String playerId;
    final private String name;
    final private TroopRequestData troopRequestData;
    final private GuildSession guildSession;

    public GuildTroopsCommandResult(String message, String playerId, String name, TroopRequestData troopRequestData,
                                    GuildSession guildSession) {
        this.message = message;
        this.playerId = playerId;
        this.name = name;
        this.troopRequestData = troopRequestData;
        this.guildSession = guildSession;
    }

    public String getMessage() {
        return message;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public TroopRequestData getTroopRequestData() {
        return this.troopRequestData;
    }

    public GuildSession getGuildSession() {
        return guildSession;
    }

    @Override
    public Object getResult() {
        return null;
    }
}
