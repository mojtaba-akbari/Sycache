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

@CacheCommandAnnotation(commandName = "LIST-NODE-MODEL",commandPattern = "",commandDescription = "List Of Nodes Models",singleCommand = "true")
public class ListNodeModelsCommand extends CacheCommand {

    public ListNodeModelsCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        String result="";

        for (Class<? extends Node> node: cache.getStorage().getAllNodesType()) {
            result+=node.getSimpleName()+"\n";
        }

        StringNode nodeResult=new StringNode("result",result);

        Vector<Node> vector=new Vector<>();
        vector.add(nodeResult);

        return new CacheResult(vector, ResultEnum.ACK);
    }
}
