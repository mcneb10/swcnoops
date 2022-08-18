package swcnoops.server.commands;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;

public class OkCommandAction extends AbstractCommandAction<CommandArguments, CommandResult> {
    @JsonIgnore
    private final String action;

    public OkCommandAction(String action) {
        this.action = action;
    }

    @Override
    protected CommandResult execute(CommandArguments arguments, long time) throws Exception {
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected CommandArguments parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerIdArguments.class);
    }

    @Override
    final public String getAction() {
        return action;
    }
}
