package org.SyCache.CacheHelperModels;

import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.Controllers.Cache;

import java.lang.reflect.InvocationTargetException;

@CacheCommandAnnotation(commandName = "NULL",commandPattern = "NULL",commandDescription = "NULL",singleCommand = "NULL")
public abstract class CacheCommand {

    protected final Cache cache;
    protected String[] pattern;

    public CacheCommand(Cache cache) {
        this.cache = cache;
    }

    public abstract CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException;

    protected void processPattern(CacheCommand command){
        pattern=command.getClass().getAnnotation(CacheCommandAnnotation.class).commandPattern().split(" ");
    }
}
