package org.SyCache.Controllers;

import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.ResultEnum;
import org.SyCache.Services.MiddleService;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class Binder {
    protected boolean isShutdown=false;

    protected MiddleService middleService;
    protected Cache cachePointer;

    protected HashMap<String, CacheCommand> commands;

    public Binder(MiddleService middleService, Cache cachePointer, HashMap<String, CacheCommand> commands) {
        this.middleService = middleService;
        this.cachePointer = cachePointer;
        this.commands = commands;
    }

    protected CacheResult commandCenter(InterfaceBaseCommandModel inputCommand) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if(inputCommand.getHeadCommand().equals(ResultEnum.QUIT.name().toLowerCase())) {
            this.isShutdown=true;
            return new CacheResult(null, ResultEnum.QUIT);
        }

        if(middleService.getCommands().containsKey(inputCommand.getHeadCommand().toLowerCase())) return middleService.getCommands().get(inputCommand.getHeadCommand().toLowerCase()).execute(inputCommand); // Execute Pattern //
        else return new CacheResult(null, ResultEnum.CMD);
    }

}
