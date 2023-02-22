package org.SyCache.CacheHelperModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.BaseNodeModel.NodeStateEnum;
import org.SyCache.CacheEntity.CacheInlineFunction;
import org.SyCache.DataNodeModels.StringNode;
import org.graalvm.polyglot.Value;

import java.util.concurrent.ConcurrentLinkedQueue;

public class CacheStream implements CacheChannel {
    private final short cacheStreamSleepTime=100;
    private final ConcurrentLinkedQueue<CacheChannelNodeStateHolder> nodeChangeFired;
    private final Value func;
    private final CacheInlineFunction cacheInlineFunction;
    private final CacheOutputDriver cacheOutputDriver;
    private final boolean isNodeSelfUpdate;
    public CacheStream(CacheInlineFunction cacheInlineFunction, CacheOutputDriver outputDriver, boolean isNodeSelfUpdate){
        this.cacheInlineFunction = cacheInlineFunction;
        this.isNodeSelfUpdate = isNodeSelfUpdate;
        this.cacheOutputDriver=outputDriver;

        this.nodeChangeFired=new ConcurrentLinkedQueue<>();

        if(cacheInlineFunction != null)
            func = cacheInlineFunction.parse();
        else func=null;
    }

    public long getStatus(){
        return this.nodeChangeFired.size();
    }

    private void installFixConfig(){
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    }

    private void hookNodeAndExecuteFunction(CacheChannelNodeStateHolder cacheChannelNodeStateHolder)  {
            try {
                String orgData = cacheChannelNodeStateHolder.getNewNode().deserializeNode();

                CacheChannelNodeStateHolder affectedNode = null;
                if (isNodeSelfUpdate && cacheInlineFunction != null) {

                    cacheChannelNodeStateHolder.getNewNode().updateNode(String.valueOf(func.execute(orgData)));

                    affectedNode = cacheChannelNodeStateHolder;
                } else if (cacheInlineFunction != null) {
                    affectedNode=new CacheChannelNodeStateHolder(cacheChannelNodeStateHolder.oldNode, new StringNode(cacheChannelNodeStateHolder.getNewNode().getKey(), String.valueOf(func.execute(orgData))),cacheChannelNodeStateHolder.getNodeStateEnum());
                }


                if (cacheOutputDriver != null) {
                    cacheOutputDriver.setCacheChannelNodeStateHolder(affectedNode != null?affectedNode:cacheChannelNodeStateHolder);
                    cacheOutputDriver.broadcast();
                }

            } catch (Exception exception) { // Continue To Working //
                System.out.println(exception.getMessage());
                cacheChannelNodeStateHolder.getNewNode().setLastNodeError(exception.getMessage());
            }
    }

    public void runInsideOfPipelineThread() throws InterruptedException {
        // Change Priority Of Stream To High //

        installFixConfig();

        while (true) {
            CacheChannelNodeStateHolder fetchNode=nodeChangeFired.poll();
            if(fetchNode != null) {
                hookNodeAndExecuteFunction(fetchNode);
            }
            else{
                Thread.sleep(cacheStreamSleepTime);
            }
        }
    }

    private void addNodeToChangeFiredQueue(CacheChannelNodeStateHolder cacheChannelNodeStateHolder) {
        this.nodeChangeFired.add(cacheChannelNodeStateHolder);
    }

    @Override
    public void insertNode(Object obj) {
        addNodeToChangeFiredQueue(new CacheChannelNodeStateHolder(null,(Node)obj, NodeStateEnum.INSERT));
    }

    @Override
    public void updateNode(Object oldObject,Object newObject) {
        addNodeToChangeFiredQueue(new CacheChannelNodeStateHolder((Node) oldObject,(Node) newObject, NodeStateEnum.UPDATE));
    }

    @Override
    public void evictNode(Object obj) {
        addNodeToChangeFiredQueue(new CacheChannelNodeStateHolder(null,(Node)obj, NodeStateEnum.EVICT));
    }

    @Override
    public void destroyNode(Object obj) {
        addNodeToChangeFiredQueue(new CacheChannelNodeStateHolder(null,(Node)obj, NodeStateEnum.DELETE));
    }

}
