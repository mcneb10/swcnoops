package swcnoops.server.commands.guild;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.guild.response.GuildGetChatKeyCommandResult;

public class GuildGetChatKey extends AbstractCommandAction<GuildGetChatKey, GuildGetChatKeyCommandResult> {
    @Override
    public String getAction() {
        return "guild.get.chatKey";
    }

    @Override
    protected GuildGetChatKeyCommandResult execute(GuildGetChatKey arguments) throws Exception {
        GuildGetChatKeyCommandResult guildGetChatKeyResponse = new GuildGetChatKeyCommandResult();
        guildGetChatKeyResponse.chatMessageEncryptionKey = "/F4yaDCcWABQFpEZf7aVqIGE1KzdZql4WGNT671Sraw=";
        return guildGetChatKeyResponse;
    }

    @Override
    protected GuildGetChatKey parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildGetChatKey.class);
    }
}
