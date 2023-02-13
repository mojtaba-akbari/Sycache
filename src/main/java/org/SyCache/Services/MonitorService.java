package org.SyCache.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.SyCache.BaseNodeModel.Node;
import org.SyCache.BasePoolModel.Pool;
import org.SyCache.CacheHelperModels.CacheResult;
import org.SyCache.CacheHelperModels.CacheTag;
import org.SyCache.CacheHelperModels.ResultEnum;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.DataNodeModels.StringNode;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.Vector;

@Component
@Scope("singleton")
@RestController
public class MonitorService implements Runnable{
    private enum STATE{
        STABLE,
        FORCE,
        AGGRESSIVE,
        BRUTAL,
        NINJA
    }

    MiddleService middleService;

    private final short maxSleepTimeWindow =1024;
    private final short minSleepTimeWindow =64;
    private short sleepTimeIndexer;

    private STATE monitorState;
    private STATE prevMonitorState;

    private Runtime runtimeVM;

    public MonitorService(MiddleService middleService) {
        this.middleService=middleService;
        this.monitorState=STATE.STABLE;
        this.prevMonitorState=STATE.STABLE;
        this.sleepTimeIndexer = maxSleepTimeWindow;
        this.runtimeVM=Runtime.getRuntime();
    }

    public String getStatusLast() throws JsonProcessingException {
        Vector<Node> nodeVector=new Vector<>();
        for (String poolName: middleService.getCache().getStorage().getPools().keySet()) {
            StringNode node=new StringNode(poolName,"PoolSize "+middleService.getCache().getStorage().getPool(poolName).size()+" NodeType "+middleService.getCache().getStorage().getPool(poolName).getNodeType().getSimpleName()+
                    " Evict Item "+middleService.getCache().getStorage().getPool(poolName).getEvictedNodes().size());
            nodeVector.add(node);
        }
        StringNode node=new StringNode("Storage","Storage Byte Size "+middleService.getCache().getRealStorageByteSize()+" TCP-Connection List "+middleService.getTcpBinderThreads().size());
        nodeVector.add(node);

        return new ObjectMapper().writeValueAsString(new CacheResult(nodeVector, ResultEnum.ACK));
    }

    @RequestMapping(path = "/monitor",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public String monitorPath(@RequestBody InterfaceBaseCommandModel command, HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, JsonProcessingException {
        return getStatusLast();
    }

    public Pool getMaxPoolEvictedSize() {
        int maxEvictPoolSize = 0;
        Pool tmpSelectedPool = null;

        for (String poolName : middleService.getCache().getStorage().getPools().keySet()) {
            Pool pool = middleService.getCache().getStorage().getPool(poolName);
            if (maxEvictPoolSize < pool.getEvictedNodes().size()) {
                maxEvictPoolSize = pool.getEvictedNodes().size();
                tmpSelectedPool = pool;
            }
        }

        return tmpSelectedPool;
    }

    public Pool getMaxPoolSize() {
        long maxPoolSize = 0;
        Pool tmpSelectedPool = null;

        for (String poolName : middleService.getCache().getStorage().getPools().keySet()) {
            Pool pool = middleService.getCache().getStorage().getPool(poolName);
            if (maxPoolSize < pool.size()) {
                maxPoolSize = pool.size();
                tmpSelectedPool = pool;
            }
        }

        return tmpSelectedPool;
    }

    private void stableStateAction(){
        if(sleepTimeIndexer != maxSleepTimeWindow)    sleepTimeIndexer = maxSleepTimeWindow;

        for (String poolName : middleService.getCache().getStorage().getPools().keySet()) {
            System.out.println("Pool [" + poolName + "] Stable Ignore Evict Items " + middleService.getCache().getStorage().getPool(poolName).getEvictedNodes().size());
            Pool tmpSelectedPool = middleService.getCache().getStorage().getPool(poolName);
            while (!tmpSelectedPool.getEvictedNodes().isEmpty()) {
                Node n = tmpSelectedPool.getEvictedNodes().poll();
                if (n != null) {
                    n.freeNodeFromEvicted();
                } else break;
            }
        }
    }

    private void forceStateAction(){
        if(sleepTimeIndexer != maxSleepTimeWindow)    sleepTimeIndexer = maxSleepTimeWindow;

        Pool tmpSelectedPool=getMaxPoolEvictedSize();

        if (tmpSelectedPool != null) {
            System.out.println("Pool [" + tmpSelectedPool.getPoolName() + "] Second Chance And Predict Check On Items " + tmpSelectedPool.getEvictedNodes().size());
            while (!tmpSelectedPool.getEvictedNodes().isEmpty()) {
                Node n = tmpSelectedPool.getEvictedNodes().poll();
                if (n != null) {
                    if (n.takeAnotherChance()) continue;
                    else if(!n.isNodeAccessOnRegularPredict()) n.cutNode();
                } else break;
            }
        }
    }

    private void aggressiveStateAction(){
        if(sleepTimeIndexer > minSleepTimeWindow) sleepTimeIndexer -= 50;

        Pool tmpSelectedPool=getMaxPoolEvictedSize();

        if (tmpSelectedPool != null) {
            System.out.println("Pool [" + tmpSelectedPool.getPoolName() + "] Destroy Aggressive With 50% Chance On Items " + tmpSelectedPool.getEvictedNodes().size());
            Random random=new Random();
            while (!tmpSelectedPool.getEvictedNodes().isEmpty()) {
                Node n = tmpSelectedPool.getEvictedNodes().poll();
                if (n != null) {
                    if(random.nextBoolean())
                        n.cutNode();
                }
                else break;
            }
        }
    }

    private void brutalStateAction(){
        if(sleepTimeIndexer > minSleepTimeWindow) sleepTimeIndexer -= 100;

        Pool tmpSelectedPool=getMaxPoolEvictedSize();

        if (tmpSelectedPool != null) {
            System.out.println("Pool [" + tmpSelectedPool.getPoolName() + "] Destroy Brutal On Items " + tmpSelectedPool.getEvictedNodes().size());
            while (!tmpSelectedPool.getEvictedNodes().isEmpty()) {
                Node n = tmpSelectedPool.getEvictedNodes().poll();
                if (n != null)
                    n.cutNode();
                else break;
            }
        }
    }

    private void ninjaStateAction(){
        if(prevMonitorState != STATE.NINJA) sleepTimeIndexer=maxSleepTimeWindow;

        int lowerIndex=0;
        int upperIndex=10;

        short counterSleep=maxSleepTimeWindow;
        while(counterSleep >= sleepTimeIndexer){
            counterSleep/=2;
            lowerIndex++;
        }

        int selectedItems=0;
        short ttlMax=0;
        Pool tmpSelectedPool=getMaxPoolSize();

        if(tmpSelectedPool != null) {

            selectedItems=(500*lowerIndex);
            ttlMax=(short)(10 * (upperIndex-lowerIndex));

            System.out.println("Pool [" + tmpSelectedPool.getPoolName() + "] Destroy Ninja On " + selectedItems+" With Min Time Live "+ ttlMax+ " Index Control "+lowerIndex);

            tmpSelectedPool.forceClearOnGroupOfItems(selectedItems, ttlMax);

            if(sleepTimeIndexer > minSleepTimeWindow) sleepTimeIndexer /=2;
            else sleepTimeIndexer=minSleepTimeWindow;
        }


    }

    @Override
    public void run() {

        while(true){
            try {
                Thread.sleep(sleepTimeIndexer);


                long totalMemory=runtimeVM.totalMemory();
                long fillMemory=runtimeVM.totalMemory()-runtimeVM.freeMemory();
                long fillPercentage = (fillMemory*100)/totalMemory;

                System.out.println("Fill Memory: "+fillPercentage+"% Prev State:"+prevMonitorState+" Monitor Sleep Time: "+sleepTimeIndexer);

                prevMonitorState=monitorState;

                if(fillPercentage >= 95)    monitorState=STATE.NINJA;
                else if (fillPercentage >= 88) monitorState=STATE.BRUTAL;
                else if ( fillPercentage >= 70) monitorState=STATE.AGGRESSIVE;
                else if (fillPercentage >= 50)  monitorState=STATE.FORCE;
                else monitorState=STATE.STABLE;


                switch (monitorState){
                    case STABLE -> stableStateAction();
                    case FORCE -> forceStateAction();
                    case AGGRESSIVE -> aggressiveStateAction();
                    case BRUTAL -> brutalStateAction();
                    case NINJA -> ninjaStateAction();
                }


            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
