package swcnoops.server.commands;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.requests.CommandResult;
import java.util.ArrayList;

public class OkListCommandAction extends OkCommandAction {

    public OkListCommandAction(String action) {
        super(action);
    }

    @Override
    protected CommandResult execute(CommandArguments arguments, long time) throws Exception {
        CommandResult listDataCommandResult = new CommandResult() {
            @JsonIgnore
            @Override
            public Integer getStatus() {
                return Integer.valueOf(0);
            }

            @JsonIgnore
            @Override
            public Object getResult() {
                return new ArrayList<>();
            }

            @JsonIgnore
            @Override
            public void setRequestPlayerId(String playerId) {

            }

            @JsonIgnore
            @Override
            public String getRequestPlayerId() {
                return null;
            }
        };

        return listDataCommandResult;
    }
}
