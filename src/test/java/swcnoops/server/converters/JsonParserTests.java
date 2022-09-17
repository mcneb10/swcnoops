package swcnoops.server.converters;

import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.response.PlayerLoginCommandResult;
import swcnoops.server.json.JacksonJsonParser;
import swcnoops.server.json.JsonParser;
import org.junit.Test;

public class JsonParserTests {

    static {
        ServiceFactory.instance(new Config());
    }

    @Test
    public void testJackson() throws Exception {
        JsonParser jsonParser = new JacksonJsonParser();
        PlayerLoginCommandResult a = jsonParser.toObjectFromResource("newPlayer/playerLogin.json", PlayerLoginCommandResult.class);
    }

    @Test
    public void unicodeTest() throws Exception {
        JsonParser jsonParser = new JacksonJsonParser();
        PlayerLoginCommandResult a = jsonParser.toObjectFromResource("newPlayer/playerLogin.json", PlayerLoginCommandResult.class);
        System.out.println(jsonParser.toJson(a));
    }
}
