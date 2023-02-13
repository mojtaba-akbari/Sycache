package org.SyCache.CacheHelperModels;

import org.SyCache.BasePoolModel.Pool;
import org.SyCache.CacheEntity.CacheInlineFunction;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;

public class CachePipeline implements Runnable {
    private CachePipelineTypeEnum pipelineType;
    private long pipelineId;
    private boolean commitState;
    private Exception exception;
    private  String[] micros;
    private final Vector<PipelineNodesHolder> microResultStorage;
    private final CachePipelineMicroActs cachePipelineMicroActs;
    private CacheStream cacheStream;
    private CacheInlineFunction cacheInlineFunction;
    private ScheduledFuture scheduledFuture;
    private Pool poolHolder;

    public CachePipeline(CachePipelineMicroActs acts, String[] micros, Pool poolHolder, CachePipelineTypeEnum pipelineType) {
        this.micros=micros;
        this.commitState=false;
        this.exception=null;
        this.poolHolder=poolHolder;
        this.pipelineType=pipelineType;
        this.cachePipelineMicroActs = acts;

        this.microResultStorage=new Vector<>();
    }

    private void executeSimpleMicroAct(String[] parts){
        cachePipelineMicroActs.hashmapActs.get(parts[0]).apply(parts,microResultStorage,this);
    }

    private void commitResult(){
        for (PipelineNodesHolder pipelineNodesHolder : microResultStorage){
            if(pipelineNodesHolder.getPairNeedCommit()) {

                pipelineNodesHolder.getNode1().commitNodeToNewState(pipelineNodesHolder.getNode2());

                // Add Node To The Task Pool //
                cachePipelineMicroActs.cache.addNodeTaskToCacheSchedulerExecutor(pipelineNodesHolder.getNode2());

            }
            else if (pipelineNodesHolder.getActionApply()){
                for(Function<String,String> func: pipelineNodesHolder.getApplyInject()){
                    func.apply("");
                }
            }
        }
    }

    public CachePipelineTypeEnum getPipelineType() {
        return pipelineType;
    }

    public void setPipelineType(CachePipelineTypeEnum pipelineType) {
        this.pipelineType = pipelineType;
    }

    public Vector<PipelineNodesHolder> getMicroResultStorage() {
        return microResultStorage;
    }

    public void setPipelineId(long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public long getPipelineId() {
        return pipelineId;
    }

    public boolean isCommitState() {
        return commitState;
    }

    public Exception getException() {
        return exception;
    }

    public String[] getMicros() {
        return micros;
    }

    public void setCacheInlineFunction(CacheInlineFunction cacheInlineFunction) {
        this.cacheInlineFunction = cacheInlineFunction;
    }

    public CacheInlineFunction getCacheInlineFunction() {
        return cacheInlineFunction;
    }

    public CacheStream getCacheStream() {
        return cacheStream;
    }

    public void setCacheStream(CacheStream cacheStream) {
        this.cacheStream = cacheStream;
    }

    public ScheduledFuture getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    @Override
    public void run() {
        try {

            int i=0;
            int jumpIndexSnapShotForControlStructure=-1;
            int microSize=micros.length-1;
            boolean lockLoopBeforeFinish=false;

            for (i=0;i<=microSize;i++) {

                String[] parts=micros[i].split(",");


                boolean controlStructureOnTopOfStack=microResultStorage.size()>0?
                        microResultStorage.get(microResultStorage.size()-1).getHolderStructure():
                        false;

                if(controlStructureOnTopOfStack){
                    PipelineNodesHolder controlStructureOnTop=microResultStorage.get(microResultStorage.size()-1);
                    switch (controlStructureOnTop.getControlStructure()){
                        case NOP -> {
                            continue; // Just Ignore Next Micro //
                        }
                        case IFVE -> {
                            if(parts[0].toUpperCase().equals(controlStructureOnTop.getControlStructure().name())){ // remove if IFVE reached
                                microResultStorage.remove(controlStructureOnTop);
                            }
                            else continue; // If this micro in IFV ignore that
                        }
                        case JMP -> {
                            microResultStorage.remove(controlStructureOnTop);
                            jumpIndexSnapShotForControlStructure=i-2; // Two Step Before because for loop increased 1
                        }
                        case LOPE -> {
                            // If Nothing Define On JMP please ignore this //
                            microResultStorage.remove(controlStructureOnTop);

                            // If Jump Location , JUMP to the location //
                            if(jumpIndexSnapShotForControlStructure != -1) {
                                i = jumpIndexSnapShotForControlStructure;
                                jumpIndexSnapShotForControlStructure = -1;
                            }
                        }
                    }
                }

                // Execute Micro //
                if(!lockLoopBeforeFinish)
                    executeSimpleMicroAct(parts);
                else lockLoopBeforeFinish=false;

                // If Micro Loop Finished But There are ControlStructure on Stack yet //
                if(i==microSize && microResultStorage.get(microResultStorage.size()-1).getHolderStructure())
                {
                    i--; // Let execute last controlStructure
                    lockLoopBeforeFinish=true; // look to prevent execute last ControlStructure
                }

            }

            // Commit All Nodes //
            commitResult();

            commitState=true;

            // Remove Pipeline //
            poolHolder.delCachePipelineOnPool(this);
        }
        catch (Exception exception){

            commitState=false;

            this.exception=exception;

        }
    }
}
