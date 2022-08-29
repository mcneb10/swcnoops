package swcnoops.server.trigger;

import swcnoops.server.ServiceFactory;

public class CommandTriggerProcessorImpl implements CommandTriggerProcessor {
    private CommandTrigger match = new MatchMakeTrigger();
    private CommandTrigger beforeWar = new BeforeWarTrigger();
    private CommandTrigger inWar = new InWarTrigger();
    private CommandTrigger beforewarend = new BeforeWarEndTrigger();
    private CommandTrigger endwar = new EndWarTrigger();
    private CommandTrigger deleteWar = new DeleteWarTrigger();
    private CommandTrigger afterCoolDownTrigger = new AfterCoolDownTrigger();

    @Override
    public void process(String playerId, String message) {
        if (!ServiceFactory.instance().getConfig().commandTriggerProcessorEnabled)
            return;

        if (message != null) {
            message = message.trim();
            message.toLowerCase();

            switch (message) {
                case "match" :
                    match.process(playerId);
                    break;
                case "beforewar" :
                    beforeWar.process(playerId);
                    break;
                case "inwar" :
                    inWar.process(playerId);
                    break;
                case "beforewarend" :
                    beforewarend.process(playerId);
                    break;
                case "endwar" :
                    endwar.process(playerId);
                    break;
                case "aftercooldown" :
                    afterCoolDownTrigger.process(playerId);
                    break;
                case "deletewar" :
                    deleteWar.process(playerId);
                    break;
            }
        }
    }
}
