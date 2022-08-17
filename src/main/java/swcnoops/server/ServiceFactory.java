package swcnoops.server;

import swcnoops.server.datasource.PlayerDataSource;
import swcnoops.server.datasource.PlayerDatasourceImpl;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.GameDataManagerImpl;
import swcnoops.server.json.GsonJsonParser;
import swcnoops.server.json.JacksonJsonParser;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.BatchProcessor;
import swcnoops.server.requests.BatchProcessorImpl;
import swcnoops.server.session.SessionManager;
import swcnoops.server.session.SessionManagerImpl;
import swcnoops.server.trigger.CommandTriggerProcessor;
import swcnoops.server.trigger.CommandTriggerProcessorImpl;
import java.util.UUID;

public class ServiceFactory {
    static private ServiceFactory instance;
    private JsonParser jsonParser;
    private Config config;
    private BatchProcessor batchProcessor;
    private SessionManager sessionManager;
    private PlayerDataSource playerDatasource;
    private GameDataManager gameDataManager;
    private CommandTriggerProcessor commandTriggerProcessor;

    static final public ServiceFactory instance() {
        return instance;
    }

    static final public ServiceFactory instance(Config config) {
        if (instance == null) {
            instance = initialise(config);
        }

        return instance;
    }

    final public BatchProcessor getBatchProcessor() {
        return batchProcessor;
    }

    final public JsonParser getJsonParser() {
        return jsonParser;
    }

    final public Config getConfig() {
        return config;
    }

    static final public String createRandomUUID() {
        return UUID.randomUUID().toString();
    }


    /**
     * SWC works in seconds from 1970
     * @return
     */
    static public long getSystemTimeSecondsFromEpoch() {
        return System.currentTimeMillis() / 1000;
    }

    final public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    final public PlayerDataSource getPlayerDatasource() {
        return this.playerDatasource;
    }

    final public GameDataManager getGameDataManager() {
        return this.gameDataManager;
    }

    static final private ServiceFactory initialise(Config config) {
        ServiceFactory newInstance = new ServiceFactory();
        newInstance.config = config;
        newInstance.batchProcessor = new BatchProcessorImpl();
        newInstance.sessionManager = new SessionManagerImpl();
        newInstance.playerDatasource = new PlayerDatasourceImpl();
        newInstance.gameDataManager = new GameDataManagerImpl();
        newInstance.commandTriggerProcessor = new CommandTriggerProcessorImpl();

        if (config.jsonParse == Config.JsonParser.Jackson)
            newInstance.jsonParser = new JacksonJsonParser();
        else
            newInstance.jsonParser = new GsonJsonParser();

        return newInstance;
    }

    final public CommandTriggerProcessor getCommandTriggerProcessor() {
        return commandTriggerProcessor;
    }
}
