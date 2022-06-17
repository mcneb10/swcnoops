package swcnoops.server;

import swcnoops.server.json.GsonJsonParser;
import swcnoops.server.json.JacksonJsonParser;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.BatchProcessor;
import swcnoops.server.requests.BatchProcessorImpl;

public class ServiceFactory {
    private JsonParser jsonParser;
    private Config config;
    private BatchProcessor batchProcessor;
    static private ServiceFactory instance;

    static final public ServiceFactory instance() {
        return instance;
    }

    static final public ServiceFactory instance(Config config) {
        if (instance == null) {
            instance = initialise(config);
        }

        return instance;
    }

    static final private ServiceFactory initialise(Config config) {
        ServiceFactory newInstance = new ServiceFactory();
        newInstance.config = config;
        newInstance.batchProcessor = new BatchProcessorImpl();

        if (config.jsonParse == Config.JsonParser.Jackson)
            newInstance.jsonParser = new JacksonJsonParser();
        else
            newInstance.jsonParser = new GsonJsonParser();

        return newInstance;
    }

    public BatchProcessor getBatchProcessor() {
        return batchProcessor;
    }

    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public Config getConfig() {
        return config;
    }

    static final public String createRandomUUID() {
        return java.util.UUID.randomUUID().toString();
    }


    /**
     * SWC works in seconds from 1970
     * @return
     */
    static public long getSystemTimeSecondsFromEpoch() {
        return System.currentTimeMillis() / 1000;
    }
}
