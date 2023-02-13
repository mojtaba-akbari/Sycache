package org.SyCache.CommandModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheCommandAnnotation;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.ResultEnum;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.Controllers.Cache;
import java.util.Vector;

@CacheCommandAnnotation(commandName = "GET",commandPattern = "KEY1 KEY2 KEY3",commandDescription = "Get Value Of Key From Pool Model",singleCommand = "false")
public class GetItemCommand extends CacheCommand {
    public GetItemCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) {

        if(!cache.getStorage().existsPool(command.getPoolName()))
            return new CacheResult(null,ResultEnum.NUL);


        Vector<Node> result=new Vector<>();
        Node NodeItem;

        for(int i=0;i<command.getTailCommand().length;i++){
            NodeItem = cache.getStorage().getPool(command.getPoolName()).getItem(command.getTailCommand()[i]);
            if(NodeItem != null) {
                if(NodeItem.getTag() != null)
                    if(!NodeItem.getTag().getValidate())    continue;

                result.add(NodeItem);
                NodeItem.accessNodeActions();
            }
        }

        return new CacheResult(result,result.size()>0?ResultEnum.ACK:ResultEnum.NUL);
    }
}
