package swcnoops.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.datasource.PlayerDataSource;
import swcnoops.server.datasource.PlayerDatasourceImpl;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.GameDataManagerImpl;
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    public static long convertJoeDate(String joeDate) {
        long date = 0;
        // 20:00,04-10-2022
        if (joeDate != null && joeDate.length() > 0) {
            try {
                String[] dateTime = joeDate.split(",");
                String utcFormat = dateTime[1] + "T" + dateTime[0] + ":00+0000";
                ZonedDateTime instant = ZonedDateTime.parse(utcFormat,
                        DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ssZ"));
                date = instant.toEpochSecond();
            } catch (Exception exception) {
                date = 0;
            }
        }

        return date;
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
        newInstance.jsonParser = new JacksonJsonParser();

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
