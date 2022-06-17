package swcnoops.server.requests;

import java.util.ArrayList;
import java.util.List;

public class LoginMessages extends CommandMessages {
    public List<Login> login;

    public LoginMessages(long messageTime, long systemTime, String messageId) {
        super(messageTime, systemTime, messageId);
        this.login = new ArrayList<>();
        Login login = new Login(messageTime, systemTime, messageId);
        this.login.add(login);
    }
}
