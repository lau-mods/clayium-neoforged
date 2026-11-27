package net.claustra01.clayium.blockentity;

/**
 * External start/stop work control contract exposed to Redstone Interfaces and Synchronized Machines.
 */
public interface IExternalControl {
    void startWork();

    void stopWork();

    boolean isDoingWork();

    boolean isScheduledWork();
}
