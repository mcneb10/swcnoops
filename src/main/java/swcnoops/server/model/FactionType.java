package swcnoops.server.model;

public enum FactionType {
    invalid,
    empire,
    rebel,
    neutral,
    smuggler,
    tusken;

    public String getNameForLookup() {
        return this.name().substring(0,1).toUpperCase() + this.name().substring(1);
    }
}
