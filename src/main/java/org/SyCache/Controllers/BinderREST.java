package org.SyCache.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.Services.MiddleService;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class BinderREST extends Binder {
    InterfaceBaseCommandModel command;

    public BinderREST(MiddleService middleService, Cache cache, HashMap<String, CacheCommand> commands, InterfaceBaseCommandModel command){
        super(middleService, cache,commands);

        this.command=command;
    }

    public String run() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, JsonProcessingException {
        return new ObjectMapper().writeValueAsString(commandCenter(command));
    }
}
