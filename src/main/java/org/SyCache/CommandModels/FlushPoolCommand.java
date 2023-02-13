package org.SyCache.CommandModels;

import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheCommandAnnotation;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.ResultEnum;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.Controllers.Cache;
import java.lang.reflect.InvocationTargetException;

@CacheCommandAnnotation(commandName = "FLUSH",commandPattern = "",commandDescription = "Flush All Items In Pool And Call GC For Faster Clear",singleCommand = "true")
public class FlushPoolCommand extends CacheCommand {
    public FlushPoolCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if(!cache.getStorage().existsPool(command.getPoolName()))
            return new CacheResult(null, ResultEnum.NUL);

        cache.getStorage().getPool(command.getPoolName()).flushPool();

        return new CacheResult(null,ResultEnum.ACK);
    }
}
