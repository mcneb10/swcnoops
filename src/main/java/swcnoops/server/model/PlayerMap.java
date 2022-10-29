package swcnoops.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerMap {
    public String planet;
    public int next;
    public Buildings buildings;
}