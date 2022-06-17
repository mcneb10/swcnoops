package swcnoops.server.model;

public class KeepAlive {
    public MessageKeepAlive message;
    public String messageId;
    public long messageTime;

    public KeepAlive(long systemTime, String messageId) {
        this.messageTime = systemTime;
        this.message = new MessageKeepAlive(systemTime);
        this.messageId = messageId;
    }
}
