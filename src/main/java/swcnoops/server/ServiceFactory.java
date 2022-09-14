package swcnoops.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.datasource.PlayerDataSource;
import swcnoops.server.datasource.PlayerDatasourceImpl;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.GameDataManagerImpl;
import swcnoops.server.json.GsonJsonParser;
import swcnoops.server.json.JacksonJsonParser;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Building;
import swcnoops.server.model.Buildings;
import swcnoops.server.requests.BatchProcessor;
import swcnoops.server.requests.BatchProcessorImpl;
import swcnoops.server.session.AuthenticationService;
import swcnoops.server.session.BatchResponseReplayer;
import swcnoops.server.session.SessionManager;
import swcnoops.server.session.SessionManagerImpl;
import swcnoops.server.trigger.CommandTriggerProcessor;
import swcnoops.server.trigger.CommandTriggerProcessorImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class ServiceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceFactory.class);

    static private ServiceFactory instance;
    private JsonParser jsonParser;
    private Config config;
    private BatchProcessor batchProcessor;
    private SessionManager sessionManager;
    private PlayerDataSource playerDatasource;
    private GameDataManager gameDataManager;
    private CommandTriggerProcessor commandTriggerProcessor;
    private AuthenticationService authenticationServer;
    private BatchResponseReplayer batchResponseReplayer;

    static final public ServiceFactory instance() {
        return instance;
    }

    static final public ServiceFactory instance(Config config) {
        if (instance == null) {
            instance = initialise(config);
        }

        return instance;
    }

    static public int getXpFromBuildings(Buildings buildings){
        ArrayList<BuildingData> buildingData = new ArrayList<BuildingData>();
        int xp =0;

        for (Building b : buildings) {
            BuildingData bbb = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(b.uid);
            buildingData.add(bbb);
        }
        BuildingData[] bd = buildingData.toArray(new BuildingData[0]);
        xp = Arrays.stream(bd).mapToInt(BuildingData::getXp).sum();


        return xp;

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
        newInstance.authenticationServer = new AuthenticationService();
        newInstance.batchResponseReplayer = new BatchResponseReplayer();

        if (config.jsonParse == Config.JsonParser.Jackson)
            newInstance.jsonParser = new JacksonJsonParser();
        else
            newInstance.jsonParser = new GsonJsonParser();

        return newInstance;
    }

    final public CommandTriggerProcessor getCommandTriggerProcessor() {
        return commandTriggerProcessor;
    }

    final public AuthenticationService getAuthenticationService() {
        return authenticationServer;
    }

    final public BatchResponseReplayer getBatchResponseReplayer() {
        return batchResponseReplayer;
    }

    public void shutdown() {
        LOG.info("Shutting down");
        this.getPlayerDatasource().shutdown();
    }
}
