package org.SyCache.Controllers;

public class InterfaceBaseCommandModel {
    String headCommand;
    String poolName;
    String[] tailCommand;

    public InterfaceBaseCommandModel() {
    }

    public InterfaceBaseCommandModel(String headCommand, String poolName, String[] tailCommand) {
        this.headCommand = headCommand;
        this.poolName = poolName;
        this.tailCommand = tailCommand;
    }

    public String getHeadCommand() {
        return headCommand;
    }

    public void setHeadCommand(String headCommand) {
        this.headCommand = headCommand;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String[] getTailCommand() {
        return tailCommand;
    }

    public void setTailCommand(String[] tailCommand) {
        this.tailCommand = tailCommand;
    }
}
