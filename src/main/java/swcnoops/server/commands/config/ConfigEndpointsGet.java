package swcnoops.server.commands.config;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.config.response.ConfigEndpointsGetCommandResult;
import swcnoops.server.requests.EmptyMessage;
import swcnoops.server.requests.Messages;

public class ConfigEndpointsGet extends AbstractCommandAction<ConfigEndpointsGet, ConfigEndpointsGetCommandResult> {
    @Override
    final public String getAction() {
        return "config.endpoints.get";
    }

    @Override
    protected ConfigEndpointsGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, ConfigEndpointsGet.class);
    }

    @Override
    protected ConfigEndpointsGetCommandResult execute(ConfigEndpointsGet arguments, long time) throws Exception {
        ConfigEndpointsGetCommandResult configEndpointsGetResponse = new ConfigEndpointsGetCommandResult();

        String ipAddress = ServiceFactory.instance().getConfig().event2BiLoggingIpAddress;
        configEndpointsGetResponse.event2BiLogging = ipAddress + "/bi_event2";
        configEndpointsGetResponse.event2NoProxyBiLogging = ipAddress + "/bi_event2";
        return configEndpointsGetResponse;
    }

    @Override
    protected Messages createMessage(Command command, ConfigEndpointsGetCommandResult commandResult) {
        return EmptyMessage.instance;
    }

    @Override
    public boolean canAttachGuildNotifications() {
        return false;
    }
}