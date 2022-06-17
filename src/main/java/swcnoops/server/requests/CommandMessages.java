package swcnoops.server.requests;

import swcnoops.server.model.Checkpoint;
import swcnoops.server.model.KeepAlive;

import java.util.ArrayList;
import java.util.List;

public class CommandMessages implements Messages {
    public List<Checkpoint> checkpoint;
    public List<KeepAlive> keepAlive;

    public CommandMessages() {
    }

    public CommandMessages(long messageTime, long systemTime, String messageId) {
        this.checkpoint = new ArrayList<>();
        this.keepAlive = new ArrayList<>();

        Checkpoint checkPoint = new Checkpoint(messageTime, messageId);
        this.checkpoint.add(checkPoint);

        KeepAlive keepAlive = new KeepAlive(systemTime, messageId);
        this.keepAlive.add(keepAlive);
    }
}
