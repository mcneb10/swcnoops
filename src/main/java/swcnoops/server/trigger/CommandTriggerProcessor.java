package swcnoops.server.trigger;

public interface CommandTriggerProcessor {
    void process(String playerId, String message);
}
