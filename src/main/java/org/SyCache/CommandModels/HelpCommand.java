package org.SyCache.CommandModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheCommandAnnotation;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.ResultEnum;
import org.SyCache.DataNodeModels.StringNode;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.Controllers.Cache;

import java.util.Vector;

@CacheCommandAnnotation(commandName = "HELP", commandPattern= "", commandDescription = "Help Command", singleCommand = "true")
public class HelpCommand extends CacheCommand {

    public HelpCommand(Cache cache) {
        super(cache);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command)  {
        Vector<Node> vector=new Vector<>();

        for (String commandKey: cache.getPtrToCommands().keySet()) {
            StringNode nodeResult=new StringNode("result",
                    "{\"headCommand\":\""+ cache.getPtrToCommands().get(commandKey).getClass().getAnnotation(CacheCommandAnnotation.class).commandName()+
                            "\",\"poolName\":\""+(Boolean.parseBoolean(cache.getPtrToCommands().get(commandKey).getClass().getAnnotation(CacheCommandAnnotation.class).singleCommand())?"":"POOL-NAME") +
                            "\",\"tailCommand\":\""+ cache.getPtrToCommands().get(commandKey).getClass().getAnnotation(CacheCommandAnnotation.class).commandPattern()+
                            "\"} "+ cache.getPtrToCommands().get(commandKey).getClass().getAnnotation(CacheCommandAnnotation.class).commandDescription()+"\n"
                    );

            vector.add(nodeResult);
        }

        return new CacheResult(vector, ResultEnum.ACK);
    }
}
