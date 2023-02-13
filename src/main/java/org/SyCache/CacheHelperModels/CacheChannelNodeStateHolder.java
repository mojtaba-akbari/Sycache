package org.SyCache.CacheHelperModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.BaseNodeModel.NodeStateEnum;

public class CacheChannelNodeStateHolder {
    protected final Node newNode;

    protected final Node oldNode;
    protected final NodeStateEnum nodeStateEnum;

    public CacheChannelNodeStateHolder(Node oldNode, Node newNode, NodeStateEnum nodeStateEnum) {
        this.newNode = newNode;
        this.oldNode = oldNode;
        this.nodeStateEnum = nodeStateEnum;
    }

    public Node getNewNode() {
        return newNode;
    }

    public Node getOldNode() {
        return oldNode;
    }

    public NodeStateEnum getNodeStateEnum() {
        return nodeStateEnum;
    }
}
