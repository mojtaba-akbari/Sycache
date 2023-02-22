package org.SyCache.BasePoolModel;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.CacheChannel;
import org.SyCache.CacheHelperModels.CacheMetadata;
import org.SyCache.CacheHelperModels.CachePipeline;
import org.SyCache.CacheHelperModels.CacheTag;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Pool implements Iterable {
    protected String poolName;
    protected int poolFixSize;

    protected Vector<CachePipeline> poolPipeline;
    protected Hashtable<String, CacheTag> tagValidateStorage;

    protected List<CacheChannel> poolChannels;
    protected Class<Node> nodeType;

    protected Constructor<Node> ptrToTheConstructor;

    private ConcurrentLinkedQueue<Node> evictedNodes;

    public Pool(String poolName, int poolFixSize, Class<Node> nodeType) throws NoSuchMethodException {
        this.poolName = poolName;
        this.poolFixSize = poolFixSize;
        this.nodeType = nodeType;

        this.tagValidateStorage=new Hashtable<>();

        this.evictedNodes = new ConcurrentLinkedQueue<>();

        this.poolPipeline =new Vector<>();

        this.poolChannels=new ArrayList<>();

        ptrToTheConstructor=nodeType.getConstructor(String.class, String.class, CacheTag.class, short.class, short.class,short.class,Vector.class, Pool.class);

        initStorage();
    }

    public String getPoolName() {
        return poolName;
    }

    public int getPoolFixSize() {
        return poolFixSize;
    }

    public Class<Node> getNodeType() {
        return nodeType;
    }

    public Hashtable<String, CacheTag> getTagValidateStorage() {
        return tagValidateStorage;
    }

    public ConcurrentLinkedQueue<Node> getEvictedNodes() {
        return evictedNodes;
    }

    public void addItemEvictedNodes(Node node) {
        evictedNodes.add(node);
    }

    public void delItemEvictedNodes(Node node){
        evictedNodes.remove(node);
    }

    public int addCachePipelineOnPool(CachePipeline pipeline){
        poolPipeline.add(pipeline);
        return poolPipeline.indexOf(pipeline);
    }

    public void delCachePipelineOnPool(CachePipeline pipeline){
        poolPipeline.remove(pipeline);
    }

    public Vector<CachePipeline> getPoolPipeline() {
        return poolPipeline;
    }

    public void addChannelToPool(CacheChannel channel){
        poolChannels.add(channel);
    }

    public void delChannelFromPool(CacheChannel channel){
        poolChannels.remove(channel);
    }

    protected void alertAddAndUpdateNodeToPoolChannel(Node oldNode,Node newNode,Boolean stateUpdate){
        for(CacheChannel cacheChannel:poolChannels)
            if(stateUpdate) cacheChannel.updateNode(oldNode,newNode);
            else cacheChannel.insertNode(newNode);
    }

    protected void alertDelNodeToPoolChannel(Node node){
        for(CacheChannel cacheChannel:poolChannels)
            cacheChannel.destroyNode(node);
    }


    public void commitItem(Node newNode, Node oldNode){

        // Link Node To Each Other //
        oldNode.setNexHistoryNodeLink(newNode);
        newNode.setPrevHistoryNodeLink(oldNode);

        // Replace Node //
        replaceNode(newNode,oldNode);
    }

    protected abstract void  initStorage();

    public abstract long size();

    public abstract int addItem(String key, String value, CacheTag tag, short valueSize, short ttl,short predictHit, Vector<CacheMetadata> metadata) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;

    public abstract Node getItem(String key);

    public abstract void replaceNode(Node newNode,Node oldNode);
    public abstract Node getAndDeleteItem(String key);

    public abstract void deleteItem(String key);

    public abstract Vector<Node> findKeys(String regexKey, int topItem);

    public abstract Vector<Node> findValues(String regexValue, int topItem);

    public abstract void flushPool();

    public abstract void forceClearOnGroupOfItems(int numberElementToClear, int cutMaxTimeToLive);

}
