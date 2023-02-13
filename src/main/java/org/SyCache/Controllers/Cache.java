package org.SyCache.Controllers;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CachePipeline;
import org.SyCache.CacheHelperModels.CachePipelineMicroActs;
import org.SyCache.CacheEntity.CacheInlineFunctionService;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Cache {
    //Auto Entity//
    CacheInlineFunctionService cacheInlineFunctionService;

    private Storage storage;

    private ScheduledExecutorService scheduledExecutorServiceNodesPTR;
    private HashMap<String, CacheCommand> ptrToCommands;

    private CachePipelineMicroActs cachePipelineMicroActs;

    long initialDelay;
    long period;
    TimeUnit timeUnit;

    private long realStorageByteSize=0;

    public Cache(ScheduledExecutorService scheduledExecutorService, CacheInlineFunctionService cacheInlineFunctionService) {
        this.scheduledExecutorServiceNodesPTR=scheduledExecutorService;

        initialDelay = 0L ;
        period = 3L ;
        timeUnit = TimeUnit.SECONDS ;

        this.storage = new Storage();

        this.cachePipelineMicroActs=new CachePipelineMicroActs(this);

        this.cacheInlineFunctionService = cacheInlineFunctionService;
    }

    public void addNodeTaskToCacheSchedulerExecutor(Node node){
        node.setScheduledFuture(scheduledExecutorServiceNodesPTR.scheduleAtFixedRate(node,initialDelay,period,timeUnit));
    }

    public void addPipelineToCacheSchedulerExecutor(CachePipeline cachePipeline){
        cachePipeline.setScheduledFuture(scheduledExecutorServiceNodesPTR.schedule(cachePipeline,initialDelay,timeUnit));
    }

    public ScheduledExecutorService getScheduledExecutorServiceNodesPTR() {
        return scheduledExecutorServiceNodesPTR;
    }

    public Storage getStorage() {
        return storage;
    }

    public void calculateSize(int changeStorage){
        this.realStorageByteSize+=changeStorage;
    }

    public long getRealStorageByteSize(){
        return realStorageByteSize;
    }

    public HashMap<String, CacheCommand> getPtrToCommands() {
        return ptrToCommands;
    }

    public void setPtrToCommands(HashMap<String, CacheCommand> ptrToCommands) {
        this.ptrToCommands = ptrToCommands;
    }

    public CachePipelineMicroActs getCachePipelineMicroActs() {
        return cachePipelineMicroActs;
    }

    public CacheInlineFunctionService getCacheStreamService() {
        return cacheInlineFunctionService;
    }
}
