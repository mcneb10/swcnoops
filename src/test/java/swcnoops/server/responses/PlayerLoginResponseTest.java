package swcnoops.server.responses;

import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.CommandAction;
import swcnoops.server.commands.player.PlayerStoreShardOffersGet;
import swcnoops.server.commands.player.response.PlayerLoginCommandResult;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseData;
import org.junit.Test;

public class PlayerLoginResponseTest {
    static {
        ServiceFactory.instance(new Config());
    }
    @Test
    public void testBatchParse() throws Exception {
        PlayerLoginCommandResult result = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource("newPlayer/playerLogin.json", PlayerLoginCommandResult.class);
        System.out.println(ServiceFactory.instance().getJsonParser().toJson(result));
    }

    @Test
    public void testFileParse() throws Exception {

    }

    @Test
    public void test() throws Exception {
        CommandAction commandAction = new PlayerStoreShardOffersGet();
        Command command = new Command();
        command.setRequestId(1L);
        command.setTime(2L);

        CommandResult result = commandAction.execute(new PlayerStoreShardOffersGet(), command.getTime());
        ResponseData responseData = commandAction.createResponse(command, result);
        String resultJson = ServiceFactory.instance().getJsonParser().toJson(responseData);
        System.out.println(resultJson);
    }
}
