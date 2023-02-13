package org.SyCache.CommandModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheCommandAnnotation;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.ResultEnum;
import org.SyCache.DataNodeModels.StringNode;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.Controllers.Cache;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

@CacheCommandAnnotation(commandName = "STAT-POOL",commandPattern = "",commandDescription = "Status Pools On Storage",singleCommand = "true")
public class StatPoolCommand extends CacheCommand {
    public StatPoolCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {

        StringNode nodeResult=null;
        Vector<Node> vector=new Vector<>();
        for (String keyPool: cache.getStorage().getPools().keySet()) {
            nodeResult=new StringNode(keyPool,"Nodes("+ cache.getStorage().getPool(keyPool).size()+")"+
                    " NodeType("+ cache.getStorage().getPool(keyPool).getNodeType().getSimpleName()+")");

            vector.add(nodeResult);
        }

        return new CacheResult(vector, ResultEnum.ACK);
    }
}
