package org.SyCache.CommandModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.BasePoolModel.Pool;
import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheCommandAnnotation;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.ResultEnum;
import org.SyCache.DataNodeModels.StringNode;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.Controllers.Cache;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

@CacheCommandAnnotation(commandName = "LIST-POOL-MODEL",commandPattern = "",commandDescription = "List Of Pools Model",singleCommand = "true")
public class ListPoolModelsCommand extends CacheCommand {
    public ListPoolModelsCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        String result="";

        for (Class<? extends Pool> pool: cache.getStorage().getAllPoolsType()) {
            result+=pool.getSimpleName()+"\n";
        }

        StringNode nodeResult=new StringNode("result",result);

        Vector<Node> vector=new Vector<>();
        vector.add(nodeResult);

        return new CacheResult(vector, ResultEnum.ACK);
    }
}
