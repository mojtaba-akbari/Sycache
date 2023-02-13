package org.SyCache.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.SyCache.CacheHelperModels.CacheCommand;
import org.SyCache.CacheHelperModels.CacheCommandAnnotation;
import org.SyCache.CacheEntity.CacheInlineFunctionService;
import org.SyCache.Controllers.BinderREST;
import org.SyCache.Controllers.BinderSocket;
import org.SyCache.Controllers.Cache;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/*
    Single Thread For Accept Connection
    This Class Accept Connection And Pass Socket To The Binder Service To
    Bind Connection And Cache Service to together

 */
@Component
@Scope("singleton")
@RestController
public class MiddleService implements  Runnable {

    private  ArrayList<BinderSocket> tcpBinderThreads;

    private ExecutorService executorConnectionPools;

    private ScheduledExecutorService scheduledExecutorServiceNodes;

    private Cache cache;

    private HashMap<String,CacheCommand> commands;

    // Auto //
    @Autowired
    CacheInlineFunctionService cacheInlineFunctionService;

    public MiddleService() {

        System.out.println("Middle Service Loaded");

        this.executorConnectionPools = Executors.newFixedThreadPool(8);
        this.scheduledExecutorServiceNodes = Executors.newScheduledThreadPool(8);

        this.tcpBinderThreads =new ArrayList<>();

        commands=new HashMap<>();
    }

    @PostConstruct
    public void postConstruct() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        this.cache = new Cache(this.scheduledExecutorServiceNodes, cacheInlineFunctionService);
        this.cache.setPtrToCommands(commands);

        Reflections reflections=new Reflections("org.SyCache.CommandModels");

        for(Class command:reflections.getTypesAnnotatedWith(CacheCommandAnnotation.class,true)){
            if(!command.getName().contains("CommandModels"))    continue;

            CacheCommand cc=(CacheCommand) (command.getConstructor(Cache.class).newInstance(cache));


            commands.put(cc.getClass().getAnnotation(CacheCommandAnnotation.class).commandName().toLowerCase(),
                    cc
            );
        }

        Thread monitorThread=new Thread(new MonitorService(this));
        monitorThread.start();

        Thread middleThread=new Thread(this);
        middleThread.start();
    }

    @RequestMapping(path = "/cache",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public String cacheCommandPath(@RequestBody InterfaceBaseCommandModel command, HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, JsonProcessingException {
        BinderREST binderREST=new BinderREST(this,this.getCache(),this.getCommands(),command);
        return binderREST.run();
    }


    @Override
    public void run() {
        try {
            ServerSocket serverSocket=new ServerSocket(7788);

            while(true){

                Socket inSocket=serverSocket.accept();

                try {
                    BinderSocket binderSocket = new BinderSocket(this,inSocket, cache, commands);

                    synchronized (tcpBinderThreads) {
                        tcpBinderThreads.add(binderSocket);
                    }

                    executorConnectionPools.execute(binderSocket);
                }catch (Exception exception){
                    System.out.println(exception.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void shutdownWorker(BinderSocket binder){
        synchronized (tcpBinderThreads) {
            tcpBinderThreads.remove(binder);
        }
    }

    public ArrayList<BinderSocket> getTcpBinderThreads() {
        return tcpBinderThreads;
    }

    public ExecutorService getExecutorConnectionPools() {
        return executorConnectionPools;
    }

    public ScheduledExecutorService getScheduledExecutorServiceNodes() {
        return scheduledExecutorServiceNodes;
    }

    public Cache getCache() {
        return cache;
    }

    public HashMap<String, CacheCommand> getCommands() {
        return commands;
    }
}
