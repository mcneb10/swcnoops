package swcnoops.server.commands;

import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.PlayerEpisodesProgressGet;
import swcnoops.server.commands.player.PlayerPreferencesSet;
import swcnoops.server.commands.player.response.PlayerEpisodesProgressGetCommandResult;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseData;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertNotNull;

public class PlayerEpisodesProgressGetTest {
    static {
        ServiceFactory.instance(new Config());
    }

    @Test
    public void test() throws Exception {
        PlayerEpisodesProgressGet action = new PlayerEpisodesProgressGet();
        PlayerEpisodesProgressGetCommandResult response = action.execute(null, 1);
        assertNotNull(response);
        String a = ServiceFactory.instance().getJsonParser().toJson(response);
        Timestamp timestamp = new Timestamp(1560645318L*1000L);
        System.out.println(timestamp);
        System.out.println(timestamp.getTime());
        //llt=1591862155
        timestamp = new Timestamp(1591862155L*1000L);
        System.out.println(timestamp);
        System.out.println(timestamp.getTime());
    }

    @Test
    public void prefer() throws Exception {
        PlayerPreferencesSet playerPreferencesSet = new PlayerPreferencesSet();
        CommandResult o = playerPreferencesSet.execute(null, 1);
        ResponseData responseData = new ResponseData();
        responseData.result = o.getResult();
        String a = ServiceFactory.instance().getJsonParser().toJson(responseData);
        System.out.println(a);
    }
}
