package org.SyCache.CommandModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheCommandAnnotation;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.ResultEnum;
import org.SyCache.Controllers.Cache;
import org.SyCache.Controllers.InterfaceBaseCommandModel;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

@CacheCommandAnnotation(commandName = "find-keys",commandPattern = "REGEX TOP",commandDescription = "Find All Keys That Match In Regex",singleCommand = "false")
public class FindKeysCommand extends CacheCommand {

    public FindKeysCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if(!cache.getStorage().existsPool(command.getPoolName()))
            return new CacheResult(null, ResultEnum.NUL);

        Vector<Node> nodeVector=cache.getStorage().getPool(command.getPoolName()).findKeys(command.getTailCommand()[0],Integer.parseInt(command.getTailCommand()[1]));
        return new CacheResult(nodeVector,nodeVector.size()>0?ResultEnum.ACK:ResultEnum.NUL);
    }
}
