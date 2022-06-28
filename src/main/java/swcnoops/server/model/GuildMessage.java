package swcnoops.server.model;

public class GuildMessage {
    private SquadMessage message;
    private String messageId;
    private long messageTime;

    public GuildMessage(SquadMessage message, String messageId, long messageTime) {
        this.message = message;
        this.messageId = messageId;
        this.messageTime = messageTime;
    }

    public SquadMessage getMessage() {
        return message;
    }

    public String getMessageId() {
        return messageId;
    }

    public long getMessageTime() {
        return messageTime;
    }
}
