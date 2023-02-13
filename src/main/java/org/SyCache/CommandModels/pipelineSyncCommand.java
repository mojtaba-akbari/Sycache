package org.SyCache.CommandModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.*;
import org.SyCache.Controllers.Cache;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.DataNodeModels.StringNode;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

@CacheCommandAnnotation(commandName = "pipeline-sync",commandPattern = "MICRO1 MICRO2 MICRO3",commandDescription = "Open Pipeline(Sync) And Run MICRO ACT And Commit All At The End",singleCommand = "false")
public class pipelineSyncCommand extends CacheCommand {
    public pipelineSyncCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if(!cache.getStorage().existsPool(command.getPoolName()))
            return new CacheResult(null, ResultEnum.NUL);

        CachePipeline cachePipeline=new CachePipeline(cache.getCachePipelineMicroActs(),command.getTailCommand(),cache.getStorage().getPool(command.getPoolName()), CachePipelineTypeEnum.SYNC);

        cachePipeline.setPipelineId(cache.getStorage().getPool(command.getPoolName()).addCachePipelineOnPool(cachePipeline));

        cachePipeline.run();

        Vector<Node> nodes=new Vector<>();
        for (PipelineNodesHolder pipelineNodesHolder :cachePipeline.getMicroResultStorage()) {
            if(!pipelineNodesHolder.getPairNeedCommit())
                nodes.add(pipelineNodesHolder.getNode2());
        }

        cache.getStorage().getPool(command.getPoolName()).delCachePipelineOnPool(cachePipeline);

        if(cachePipeline.isCommitState())
            return new CacheResult(nodes.size()>0?nodes:null,ResultEnum.ACK);
        else {
            nodes.add(new StringNode("Exception",cachePipeline.getException().getMessage()));
            return new CacheResult(nodes,ResultEnum.EXP);
        }
    }
}
