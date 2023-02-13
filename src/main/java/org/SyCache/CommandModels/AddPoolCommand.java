package org.SyCache.CommandModels;

import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheCommandAnnotation;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.ResultEnum;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.Controllers.Cache;
import java.lang.reflect.InvocationTargetException;

@CacheCommandAnnotation(commandName = "ADD-POOL",commandPattern = "PoolFixSize PoolClass NodeClass",commandDescription = "Add Pool To The Storage with  Pool And Node Model",singleCommand = "false")
public class AddPoolCommand extends CacheCommand {
    public AddPoolCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        if(!cache.getStorage().existsPool(command.getPoolName())) {

            if(cache.getStorage().addPool(command.getPoolName(),Integer.parseInt(command.getTailCommand()[0]),command.getTailCommand()[1],command.getTailCommand()[2]))
                return new CacheResult(null, ResultEnum.ACK);
            else return new CacheResult(null, ResultEnum.NOF);
        }
        else
            return new CacheResult(null, ResultEnum.DUP);

    }
}
