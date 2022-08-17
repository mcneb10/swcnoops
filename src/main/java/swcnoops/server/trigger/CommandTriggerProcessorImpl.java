package swcnoops.server.trigger;

import swcnoops.server.ServiceFactory;

public class CommandTriggerProcessorImpl implements CommandTriggerProcessor {
    private CommandTrigger match = new MatchMakeTrigger();

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
            }
        }
    }
}
