package swcnoops.server;

public class Config {
    public int PROTOCOL_VERSION = 93;
    public int webServicePort = 8080;

    /**
     * Change this to pick up different templates
     */
    public String templateDir = "existingPlayer";
    public String playerLoginTemplate = templateDir + "/" + "playerLogin.json";
    public String playerContentGetTemplate = templateDir + "/" + "playerContentGet.json";
    public String guildGetTemplate = templateDir + "/" + "guildGet.json";
    public String guildWarGetParticipantTemplate = templateDir + "/" + "guildWarGetParticipant.json";

    public JsonParser jsonParse = JsonParser.Jackson;
    public String swcFolderName = "swcFiles";
    public String swcRootPath = "c:/swcnoops/";
    public String layoutsPath = swcRootPath + "layouts";
    public String event2BiLoggingIpAddress = "https://swc-bi-prod.apps.starwarscommander.com";
    public String playerSqliteDB = "jdbc:sqlite:" + swcRootPath + "players.db";
    public String playerCreatePlayerDBSqlResource = "sqlite/createPlayerSqliteTable.sql";

    public enum JsonParser {Jackson, Gson}
}
