package swcnoops.server.requests;

import swcnoops.server.model.GuildMessage;

import java.util.ArrayList;
import java.util.List;

public class GuildMessages extends CommandMessages {
    public List<GuildMessage> guild = new ArrayList<>();

    public GuildMessages(long messageTime, long systemTime, String messageId) {
        super(messageTime, systemTime, messageId);
    }

    public List<GuildMessage> getGuild() {
        return guild;
    }
}
