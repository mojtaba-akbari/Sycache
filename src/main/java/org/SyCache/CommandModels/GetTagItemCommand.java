package org.SyCache.CommandModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.*;
import org.SyCache.Controllers.Cache;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

@CacheCommandAnnotation(commandName = "GET-TAG",commandPattern = "TAG",commandDescription = "Get Nodes With Tag From Pool Model",singleCommand = "false")
public class GetTagItemCommand extends CacheCommand {
    public GetTagItemCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if(!cache.getStorage().existsPool(command.getPoolName()))
            return new CacheResult(null, ResultEnum.NUL);

        Vector<Node> result=null;

        CacheTag tag=cache.getStorage().getPool(command.getPoolName()).getTagValidateStorage().get(command.getTailCommand()[0]);
        if(tag != null)
            if(tag.getValidate())
                result=cache.getStorage().getPool(command.getPoolName()).getTagValidateStorage().get(command.getTailCommand()[0]).NodePTRHolder();

        return new CacheResult(result,result != null?ResultEnum.ACK:ResultEnum.INV);
    }
}
