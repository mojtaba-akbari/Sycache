package org.SyCache.CacheHelperModels;

import org.SyCache.BaseNodeModel.Node;

import java.util.LinkedList;
import java.util.function.Function;

public class PipelineNodesHolder {
    private Node node1;
    private Node node2;

    private LinkedList<Function<String,String>> applyInject;

    private Boolean isPairNeedCommit=false;
    private Boolean isActionApply=false;

    private Boolean isHolderStructure=false;

    private PipelineNodesHolderControlStructureEnum controlStructure;

    public PipelineNodesHolder(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public Node getNode1() {
        return node1;
    }

    public Node getNode2() {
        return node2;
    }

    public void setNode1(Node node1) {
        this.node1 = node1;
    }

    public void setNode2(Node node2) {
        this.node2 = node2;
    }

    public void setPairNeedCommit(Boolean pairNeedCommit) {
        isPairNeedCommit = pairNeedCommit;
    }

    public void setActionApply(Boolean actionApply) {
        isActionApply = actionApply;

        if(actionApply)
            this.applyInject=new LinkedList<>();
    }

    public LinkedList<Function<String, String>> getApplyInject() {
        return applyInject;
    }

    public Boolean getPairNeedCommit() {
        return isPairNeedCommit;
    }

    public Boolean getActionApply() {
        return isActionApply;
    }

    public Boolean getHolderStructure() {
        return isHolderStructure;
    }

    public void setHolderStructure(Boolean holderStructure) {
        isHolderStructure = holderStructure;
    }

    public PipelineNodesHolderControlStructureEnum getControlStructure() {
        return controlStructure;
    }

    public void setControlStructure(PipelineNodesHolderControlStructureEnum controlStructure) {
        this.controlStructure = controlStructure;
    }
}
