package swcnoops.server.commands.player.response;

import swcnoops.server.requests.AbstractCommandResult;

import java.util.List;

public class PlayerContentGetCommandResult extends AbstractCommandResult {
    public List<String> cdnRoots;
    public String manifest;
    public int manifestVersion;
    public List<String> patches;
    public List<String> secureCdnRoots;
}
