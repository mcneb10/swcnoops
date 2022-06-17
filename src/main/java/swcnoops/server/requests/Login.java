package swcnoops.server.requests;

import swcnoops.server.model.MessageLogin;

public class Login {
    public MessageLogin message;
    public String messageId;
    public long messageTime;

    public Login(long loginTime, long messageTime, String messageId) {
        this.message = new MessageLogin(loginTime);
        this.messageTime = messageTime;
        this.messageId = messageId;
    }
}
