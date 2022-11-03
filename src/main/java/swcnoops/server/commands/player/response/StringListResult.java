package swcnoops.server.commands.player.response;

import swcnoops.server.requests.AbstractCommandResult;

import java.util.ArrayList;
import java.util.List;

public class StringListResult extends AbstractCommandResult {
    private List<String> data = new ArrayList<>();

    @Override
    public Object getResult() {
        return this.data;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
