package swcnoops.server.commands.player.response;

import swcnoops.server.model.Grind;
import swcnoops.server.requests.AbstractCommandResult;

/**
 * not sure about this one yet
 */
public class PlayerEpisodesProgressGetCommandResult extends AbstractCommandResult {
    public String uid;
    public Long endTime;
    public Object finishedTasks;
    public int currentTaskIndex;
    public boolean introStoryViewed;
    public Object currentTask;
    public Grind grind;
}
