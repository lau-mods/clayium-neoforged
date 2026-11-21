package net.claustra01.clayium.machine.miner;

public enum BasicOperationType {
    BLOCK_BREAKER("block_breaker"),
    ACTIVATOR("activator");

    private final String id;

    BasicOperationType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
