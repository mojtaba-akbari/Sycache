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

@CacheCommandAnnotation(commandName = "STAT-STORAGE",commandPattern = "",commandDescription = "Status Storage",singleCommand = "true")
public class StatStorageCommand extends CacheCommand {
    public StatStorageCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {

        Vector<Node> vector=new Vector<>();
        StringNode nodeResult=new StringNode("storage","Storage Byte Size :"+
                cache.getRealStorageByteSize());

        vector.add(nodeResult);

        return new CacheResult(vector, ResultEnum.ACK);
    }
}
