package swcnoops.server.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;

abstract public class AbstractCommandResult implements CommandResult {
    // transient so gson ignores it, default is true as if we are creating
    // a response then it is working
    @JsonIgnore
    transient private boolean success = true;
    @JsonIgnore
    @Override
    public Integer getStatus() {
        if (isSuccess())
            return Integer.valueOf(0);

        return Integer.valueOf(-1);
    }

    public boolean isSuccess() {
        return success;
    }
    @JsonIgnore
    public void setSuccess(boolean success) {
        this.success = success;
    }
    @JsonIgnore
    @Override
    public Object getResult() {
        return this;
    }
}
