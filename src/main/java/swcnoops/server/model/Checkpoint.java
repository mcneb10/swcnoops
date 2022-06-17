package swcnoops.server.model;

public class Checkpoint {
    public MessageCheckPoint message;
    public String messageId;
    public long messageTime;

    public Checkpoint(long messageTime, String messageId) {
        this.messageTime = messageTime;
        this.message = new MessageCheckPoint(messageTime);
        this.messageId = messageId;
    }
}
