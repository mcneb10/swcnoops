package swcnoops.server.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface CommandResult {
    Integer getStatus();

    Object getResult();

    @JsonIgnore
    void setRequestPlayerId(String playerId);
    @JsonIgnore
    String getRequestPlayerId();
}
