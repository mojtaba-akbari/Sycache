package org.SyCache.CommandModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheCommandAnnotation;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.ResultEnum;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.Controllers.Cache;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

@CacheCommandAnnotation(commandName = "DEL",commandPattern = "KEY1 KEY2 KEY3",commandDescription = "Retrieve And Delete Key And Value From Pool Model",singleCommand = "false")
public class DeleteItemCommand extends CacheCommand {
    public DeleteItemCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if(!cache.getStorage().existsPool(command.getPoolName()))
            return new CacheResult(null, ResultEnum.NUL);


        Vector<Node> result=new Vector<>();
        Node byteNodeItem =null;

        for(int i=0;i<command.getTailCommand().length;i++){
            byteNodeItem = cache.getStorage().getPool(command.getPoolName()).getAndDeleteItem(command.getTailCommand()[i]);
            if(byteNodeItem != null)
                result.add(byteNodeItem);
        }

        return new CacheResult(result,result.size()>0?ResultEnum.ACK:ResultEnum.NUL);
    }
}
