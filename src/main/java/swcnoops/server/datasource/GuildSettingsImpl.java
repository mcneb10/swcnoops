package swcnoops.server.datasource;

import swcnoops.server.commands.guild.GuildHelper;
import swcnoops.server.model.FactionType;
import swcnoops.server.model.Member;
import swcnoops.server.model.Perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildSettingsImpl implements GuildSettings {
    private String id;
    private String name;
    private String description;
    private FactionType faction;

    private Map<String, String> memberMap = new HashMap<>();
    final private List<Member> members = new ArrayList<>();
    private boolean openEnrollment;
    private Integer minScoreAtEnrollment;
    private String icon;

    public GuildSettingsImpl(String id) {
        this.id = id;
    }

    @Override
    public String getGuildId() {
        return id;
    }

    @Override
    public String getGuildName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public FactionType getFaction() {
        return faction;
    }

    // TODO
    @Override
    public long getCreated() {
        return 0;
    }

    @Override
    public Perks getPerks() {
        return GuildHelper.emptyPerks;
    }

    @Override
    public List<Member> getMembers() {
        return this.members;
    }

    @Override
    public String getIcon() {
        return this.icon;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected void setFaction(FactionType faction) {
        this.faction = faction;
    }

    protected void addMember(String playerId, String playerName) {
        if (!this.memberMap.containsKey(playerId)) {
            this.memberMap.put(playerId, playerName);
            this.members.add(GuildHelper.createMember(playerId, playerName));
        }
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public void setMinScoreAtEnrollment(Integer minScoreAtEnrollment) {
        this.minScoreAtEnrollment = minScoreAtEnrollment;
    }

    @Override
    public void setOpenEnrollment(boolean openEnrollment) {
        this.openEnrollment = openEnrollment;
    }

    @Override
    public boolean canEdit() {
        return true;
    }
}
