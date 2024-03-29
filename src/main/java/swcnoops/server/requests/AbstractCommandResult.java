package swcnoops.server.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;

abstract public class AbstractCommandResult implements CommandResult {
    // transient so gson ignores it, default is true as if we are creating
    // a response then it is working
    @JsonIgnore
    transient private boolean success = true;
    private Integer errorCode = -1;
    @JsonIgnore
    @Override
    public Integer getStatus() {
        if (isSuccess())
            return Integer.valueOf(ResponseHelper.RECEIPT_STATUS_COMPLETE);

        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    protected boolean isSuccess() {
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
