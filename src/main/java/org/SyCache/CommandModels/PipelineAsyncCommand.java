package org.SyCache.CommandModels;

import org.SyCache.CacheHelperModels.*;
import org.SyCache.Controllers.Cache;
import org.SyCache.Controllers.InterfaceBaseCommandModel;

import java.lang.reflect.InvocationTargetException;

@CacheCommandAnnotation(commandName = "pipeline-async",commandPattern = "MICRO1 MICRO2 MICRO3",commandDescription = "Open Pipeline(Async) And Run MICRO ACT And Commit All At The End",singleCommand = "false")
public class PipelineAsyncCommand extends CacheCommand {
    public PipelineAsyncCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if(!cache.getStorage().existsPool(command.getPoolName()))
            return new CacheResult(null, ResultEnum.NUL);

        CachePipeline cachePipeline=new CachePipeline(cache.getCachePipelineMicroActs(),command.getTailCommand(),cache.getStorage().getPool(command.getPoolName()), CachePipelineTypeEnum.ASYNC);

        cachePipeline.setPipelineId(cache.getStorage().getPool(command.getPoolName()).addCachePipelineOnPool(cachePipeline));


        cache.addPipelineToCacheSchedulerExecutor(cachePipeline);

        return new CacheResult(null,ResultEnum.ACK);
    }
}
