package swcnoops.server.commands.guild.response;

import swcnoops.server.requests.AbstractCommandResult;

import java.util.HashMap;
import java.util.Map;

public class GuildWarGetBaseStatusResult extends AbstractCommandResult {
    public String buffUid;
    public String ownerId;
    public int level;
    public Map<String, Long> currentlyDefending = new HashMap<>();
}
