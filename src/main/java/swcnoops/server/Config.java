package swcnoops.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.util.Properties;

public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(SparkMain.class);

    public Integer PROTOCOL_VERSION = 93;
    public Integer webServicePort = 8080;

    /**
     * Change this to pick up different templates
     */
    public String templateDir = "newPlayer";
    public String playerLoginTemplate = templateDir + "/" + "playerLogin.json";
    public String playerContentGetTemplate = templateDir + "/" + "playerContentGet.json";
    public String guildWarGetParticipantTemplate = templateDir + "/" + "guildWarGetParticipant.json";

    public JsonParser jsonParse = JsonParser.Jackson;
    public String swcFolderName = "swcFiles";
    public String swcRootPath = "c:/swcnoops/";
    public String layoutsPath = swcRootPath + "layouts";
    public String event2BiLoggingIpAddress = "http://192.168.1.142:8080";
    public String playerSqliteDB = "jdbc:sqlite:" + swcRootPath + "players.db";
    public String playerCreatePlayerDBSqlResource = "sqlite/createPlayerSqliteTable.sql";

    public String troopJson = "patches/manifest45/trp.json";
    public String baseJson = "patches/manifest45/base.json";
    public String caeJson = "patches/manifest45/cae.json";
    public String cdnRoots = "http://192.168.1.142:8080/swcFiles/";
    public boolean createBotPlayersInGroup = true;
    public boolean commandTriggerProcessorEnabled = true;
    public long attackDuration = 60 * 2;            // 2 minutes
    public long warPlayerPreparationDuration = 60 * 60 * 24;     // war players prep time
    public long warServerPreparationDuration = 60 * 2;     // war server prep time
    public long warPlayDuration = 60 * 60 * 23;            // war duration
    public long warResultDuration = 60 * 2;                // server result duration
    public long warCoolDownDuration = 60 * 60 * 24;        // war cool down
    public int batchResponseReplayWait = 1000 * 5;
    public boolean enableBatchResponseReplayer = false;
    public boolean freeResources = true;
    public boolean loadDevBases = true;
    public boolean handleMissingAccounts = true;
    public String mongoDBConnection = "mongodb+srv://user:password@localhost/?retryWrites=true&w=majority";

    public void setFromProperties(Properties properties) throws Exception {
        Class<?> clazz = this.getClass();
        for (Field field : clazz.getFields()) {
            String fieldName = field.getName();
            if (properties.containsKey(fieldName)) {
                set(field, this, properties.getProperty(fieldName));
            }
        }

        LOG.info("********* Configured values **********");
        for (Field field : clazz.getFields()) {
            String fieldName = field.getName();
            LOG.info(String.format("Property %s = %s", fieldName, field.get(this)));
        }
        LOG.info("**************************************");
    }

    private void set(Field field, Config config, String property) throws Exception {
        Object value = convertToObject(property, field.getType());
        field.set(config, value);
    }

    private Object convertToObject(String property, Class<?> type) {
        if (type == Integer.class) {
            return Integer.valueOf(property);
        } else if (type == String.class) {
            return property;
        }

        return null;
    }

    public enum JsonParser {Jackson, Gson}
}
