package swcnoops.server.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ResponseHelper {
    static final public int STATUS_CODE_NOT_MODIFIED = 14;
    static final public int STATUS_CODE_BAD_INPUT = 701;

    // this status is what flags up another login from another device
    static final public int LOGIN_TIME_MISMATCH = 917;
    static final public int STATUS_CODE_EXTERNAL_ACCOUNT_AUTH_FAILURE = 1318;
    static final public int INACTIVE_PLAYER_IDENTITY = 1900;
    static final public int DESYNC_BANNED = 1999;
    static final public int STATUS_CODE_PVP_TARGET_IS_UNDER_PROTECTION = 2100;
    static final public int STATUS_CODE_PVP_TARGET_IS_UNDER_ATTACK = 2101;
    static final public int STATUS_CODE_PVP_TARGET_IS_ONLINE = 2102;
    static final public int STATUS_CODE_PVP_TARGET_NOT_FOUND = 2103;
    static final public int STATUS_CODE_PVP_TARGET_BANNED = 2104;
    static final public int STATUS_CODE_PVP_TARGET_IS_INVALID = 2105;
    static final public int STATUS_CODE_PVP_TARGET_PLANET_MISMATCH = 2106;
    static final public int STATUS_CODE_PVP_TARGET_HAS_RELOCATED = 2107;
    static final public int REPLAY_DATA_NOT_FOUND = 2110;
    static final public int STATUS_CODE_ALREADY_REGISTERED = 2200;
    static final public int STATUS_CODE_PERMANENTLY_LINKED = 2201;
    static final public int STATUS_CODE_EXTERNAL_ACCOUNT_IS_SAME = 2202;
    static final public int STATUS_CODE_GUILD_MIN = 2300;
    static final public int STATUS_CODE_ALREADY_IN_A_GUILD = 2300;
    static final public int STATUS_CODE_GUILD_NAME_TAKEN = 2301;
    static final public int STATUS_CODE_GUILD_IS_FULL = 2302;
    static final public int STATUS_CODE_GUILD_NOT_OPEN_ENROLLMENT = 2303;
    static final public int STATUS_CODE_GUILD_SCORE_REQ_NOT_MET = 2304;
    static final public int STATUS_CODE_GUILD_WRONG_FACTION = 2305;
    static final public int STATUS_CODE_NOT_IN_GUILD = 2306;
    static final public int STATUS_CODE_NOT_ENOUGH_GUILD_RANK = 2309;
    static final public int STATUS_CODE_NOT_IN_SAME_GUILD = 2315;
    static final public int STATUS_CODE_CANNOT_DEDUCT_NEGATIVE_AMOUNT = 2316;
    static final public int STATUS_CODE_CAN_ONLY_DONATE_TROOPS = 2318;
    static final public int STATUS_CODE_NOT_ENOUGH_GUILD_TROOP_CAPACITY = 2319;
    static final public int STATUS_CODE_TOO_SOON_TO_REQUEST_TROOPS_AGAIN = 2320;
    static final public int STATUS_CODE_PLAYER_IS_IN_SQUAD_WAR = 2321;
    static final public int STATUS_CODE_GUILD_MAX = 2322;
    // this one looks like it is for outpost
    static final public int STATUS_CODE_GUILD_WAR_BASE_ALREADY_OWNED = 2402;
    static final public int STATUS_CODE_GUILD_WAR_BASE_UNDER_ATTACK = 2403;
    static final public int STATUS_CODE_GUILD_WAR_PARTICIPANT_IN_ATTACK = 2404;
    static final public int STATUS_CODE_GUILD_WAR_NOT_ENOUGH_TURNS = 2406;
    static final public int STATUS_CODE_GUILD_WAR_NOT_ENOUGH_VICTORY_POINTS = 2407;
    static final public int STATUS_CODE_GUILD_WAR_WRONG_PHASE = 2409;
    static final public int STATUS_CODE_GUILD_WAR_CANNOT_CLAIM_EXPIRED_REWARD = 2413;
    static final public int STATUS_CODE_GUILD_WAR_EXPIRED = 2414;
    static final public int STATUS_CODE_GUILD_WAR_PLAYER_UNDER_ATTACK = 2418;
    static final public int STATUS_CODE_GUILD_WAR_BUFF_BASE_OWNER_CHANGE = 2421;
    static final public int STATUS_CODE_CANNOT_UNLOCK_ALREADY_AVAILABLE_PERK = 2502;
    static final public int STATUS_CODE_CANNOT_UPGRADE_PERK_NONSEQUENTIALLY = 2504;
    static final public int DEACTIVATE_EQUIPMENT_FAILED = 2604;
    static final public int RECEIPT_STATUS_COMPLETE = 0;
    static final public int RECEIPT_STATUS_INITIATED = 6300;

    final static public CommandResult SUCCESS_NULL_COMMAND_RESULT = new AbstractCommandResult() {
        @Override
        public Integer getStatus() {
            return Integer.valueOf(ResponseHelper.RECEIPT_STATUS_COMPLETE);
        }

        @JsonIgnore
        @Override
        public Object getResult() {
            return null;
        }
    };

    final static public CommandResult SUCCESS_COMMAND_RESULT = new AbstractCommandResult() {
        @JsonIgnore
        @Override
        public Integer getStatus() {
            return Integer.valueOf(ResponseHelper.RECEIPT_STATUS_COMPLETE);
        }

        @JsonIgnore
        @Override
        public Object getResult() {
            return this;
        }
    };

    final static public CommandResult FAILED_COMMAND_RESULT = new AbstractCommandResult() {
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

    public static CommandResult newErrorResult(int statusCodePvpTargetNotFound) {
        return new CommandResult() {
            @Override
            public Integer getStatus() {
                return statusCodePvpTargetNotFound;
            }

            @Override
            public Object getResult() {
                return null;
            }
        };
    }

    static private class JsonElementCommandResult extends AbstractCommandResult {
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

    static private class StringCommandResult extends AbstractCommandResult {
        final private String result;
        final private boolean isSuccess;
        public StringCommandResult(String result, boolean isSuccess) {
            this.result = result;
            this.isSuccess = isSuccess;
        }

        @Override
        public Integer getStatus() {
            if (isSuccess)
                return Integer.valueOf(ResponseHelper.RECEIPT_STATUS_COMPLETE);

            return Integer.valueOf(1);
        }

        @JsonIgnore
        @Override
        public Object getResult() {
            return result;
        }
    }
}
