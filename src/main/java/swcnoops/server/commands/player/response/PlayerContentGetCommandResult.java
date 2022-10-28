package swcnoops.server.commands.player.response;

import swcnoops.server.requests.AbstractCommandResult;

import java.util.ArrayList;
import java.util.List;

public class PlayerContentGetCommandResult extends AbstractCommandResult {
    public List<String> cdnRoots = new ArrayList<>();
    public String manifest;
    public String manifestVersion;
    public List<String> patches = new ArrayList<>();
    public List<String> secureCdnRoots = new ArrayList<>();
}
