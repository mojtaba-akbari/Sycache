package org.SyCache.CommandModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.*;
import org.SyCache.Controllers.Cache;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.DataNodeModels.StringNode;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

@CacheCommandAnnotation(commandName = "LIST-PIPELINE",commandPattern = "",commandDescription = "List pipeline on one pool",singleCommand = "true")
public class ListPoolPipelineCommand extends CacheCommand {
    public ListPoolPipelineCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if(!cache.getStorage().existsPool(command.getPoolName()))
            return new CacheResult(null, ResultEnum.NUL);


        Vector<Node> result=new Vector<>();

        for (CachePipeline cachePipeline:cache.getStorage().getPool(command.getPoolName()).getPoolPipeline()){
            StringNode node =new StringNode(String.join(",",cachePipeline.getMicros()),"Pipeline ID ["+cachePipeline.getPipelineId()+"] "+
                    "CommitState ["+cachePipeline.isCommitState()+"] Exception ["+cachePipeline.getException()+"]"
                    );

            result.add(node);
        }


        return new CacheResult(result,result.size()>0?ResultEnum.ACK:ResultEnum.NUL);
    }
}
