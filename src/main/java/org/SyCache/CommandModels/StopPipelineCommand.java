package org.SyCache.CommandModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.*;
import org.SyCache.Controllers.Cache;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.DataNodeModels.StringNode;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

@CacheCommandAnnotation(commandName = "STOP-PIPELINE",commandPattern = "PipelineID",commandDescription = "Stop Open Pipeline on One Pool",singleCommand = "false")
public class StopPipelineCommand extends CacheCommand {

    public StopPipelineCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if(!cache.getStorage().existsPool(command.getPoolName()))
            return new CacheResult(null, ResultEnum.NUL);

        Vector<Node> result=new Vector<Node>();
        CachePipeline cachePipeline=cache.getStorage().getPool(command.getPoolName()).getPoolPipeline().get(Integer.parseInt(command.getTailCommand()[0]));

        StringNode node =new StringNode(String.join(" ",cachePipeline.getMicros()),"Pipeline ID ["+cachePipeline.getPipelineId()+"] "+
                "CommitState ["+cachePipeline.isCommitState()+"] Exception ["+cachePipeline.getException()+"] % Force To Interrupt %"
        );

        if(cachePipeline.getScheduledFuture() != null)
            cachePipeline.getScheduledFuture().cancel(true);

        if(cachePipeline.getCacheStream() != null)
            cache.getStorage().getPool(command.getPoolName()).delChannelFromPool(cachePipeline.getCacheStream());

        cache.getStorage().getPool(command.getPoolName()).getPoolPipeline().remove(cachePipeline);

        result.add(node);

        return new CacheResult(result,ResultEnum.ACK);
    }
}
