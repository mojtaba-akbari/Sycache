package org.SyCache.Controllers;


import org.SyCache.BaseNodeModel.Node;
import org.SyCache.BasePoolModel.Pool;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

public class Storage {

    // Array List Of Pools //
    private HashMap<String, Pool> pools;

    Set<Class<? extends Node>> allNodesType;
    Set<Class<? extends Pool>> allPoolType;

    public Storage() {
        this.pools = new HashMap<>();
        // Fully Dynamically Load Pool Type And Node Type //
        Reflections reflectNodePackage= new Reflections("org.SyCache.DataNodeModels");
        Reflections reflectPoolPackage= new Reflections("org.SyCache.DataPoolModels");

        allNodesType = reflectNodePackage.getSubTypesOf(Node.class);
        allPoolType = reflectPoolPackage.getSubTypesOf(Pool.class);
    }

    public boolean addPool(String poolName, int poolFixSize, String poolClass,String nodeClass) throws
            NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Class<Node> nodeClassT=null;
        for (Class<? extends Node> cn:allNodesType) {
            if(cn.getSimpleName().equals(nodeClass)) nodeClassT=(Class<Node>) cn;
        }

        Class<Pool> poolCLassT=null;

        for (Class<? extends Pool> cp:allPoolType) {
            if(cp.getSimpleName().equals(poolClass)) poolCLassT=(Class<Pool>) cp;
        }

        if(poolCLassT != null && nodeClassT != null) {
            Pool pool = poolCLassT.getConstructor(String.class, int.class, Class.class).newInstance(poolName, poolFixSize, nodeClassT);
            pools.put(poolName, pool);
            return true;
        }
        else return false;
    }

    public void delPool(String poolName){
        pools.remove(poolName);
    }

    public boolean existsPool(String poolName){
        return pools.containsKey(poolName);
    }

    public Pool getPool(String poolName){
        return pools.get(poolName);
    }

    public HashMap<String, Pool> getPools() {
        return pools;
    }

    public Set<Class<? extends Node>> getAllNodesType() {
        return allNodesType;
    }

    public Set<Class<? extends Pool>> getAllPoolsType() {
        return allPoolType;
    }
}
