package swcnoops.server.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ResponseHelper {
    final static public CommandResult SUCCESS_NULL_COMMAND_RESULT = new CommandResult() {
        @Override
        public Integer getStatus() {
            return Integer.valueOf(0);
        }

        @JsonIgnore
        @Override
        public Object getResult() {
            return null;
        }
    };

    final static public CommandResult SUCCESS_COMMAND_RESULT = new CommandResult() {
        @JsonIgnore
        @Override
        public Integer getStatus() {
            return Integer.valueOf(0);
        }

        @JsonIgnore
        @Override
        public Object getResult() {
            return this;
        }
    };

    final static public CommandResult FAILED_COMMAND_RESULT = new CommandResult() {
        @JsonIgnore
        @Override
        public Integer getStatus() {
            return Integer.valueOf(-1);
        }

        @JsonIgnore
        @Override
        public Object getResult() {
            return this;
        }
    };

    public static CommandResult newStringResponse(String stringResult, boolean isSuccess) {
        return new StringCommandResult(stringResult, isSuccess);
    }

    public static CommandResult newJsonElementResponse(Object jsonElement) {
        return new JsonElementCommandResult(jsonElement);
    }

    static private class JsonElementCommandResult implements CommandResult {
        final private Object result;

        public JsonElementCommandResult(Object result) {
            this.result = result;
        }

        @Override
        public Integer getStatus() {
            return Integer.valueOf(0);
        }

        @JsonIgnore
        @Override
        public Object getResult() {
            return result;
        }
    }

    static private class StringCommandResult implements CommandResult {
        final private String result;
        final private boolean isSuccess;
        public StringCommandResult(String result, boolean isSuccess) {
            this.result = result;
            this.isSuccess = isSuccess;
        }

        @Override
        public Integer getStatus() {
            if (isSuccess)
                return Integer.valueOf(0);

            return Integer.valueOf(1);
        }

        @JsonIgnore
        @Override
        public Object getResult() {
            return result;
        }
    }
}
