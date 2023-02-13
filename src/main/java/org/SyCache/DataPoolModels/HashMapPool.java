package org.SyCache.DataPoolModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.BasePoolModel.Pool;
import org.SyCache.CacheHelperModels.CacheMetadata;
import org.SyCache.CacheHelperModels.CacheTag;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class HashMapPool extends Pool {

    private ConcurrentHashMap<String, Node> poolMap;


    public HashMapPool(String poolName, int poolFixSize,Class<Node> nodeType) throws NoSuchMethodException {
        super(poolName,poolFixSize,nodeType);
    }

    @Override
    protected void initStorage() {
        this.poolMap = new ConcurrentHashMap<>(this.poolFixSize);
    }

    @Override
    public int addItem(String key, String value, CacheTag tag, short valueSize, short ttl,short predictHit,Vector<CacheMetadata> metadata) throws InvocationTargetException, InstantiationException, IllegalAccessException {

        Node newNode= ptrToTheConstructor.newInstance(key,value, tag, valueSize,ttl, predictHit, metadata, this);
        Node oldNode=null;

        int lastSize=0;
        boolean isUpdate=false;

        if(!poolMap.containsKey(key)) {
            poolMap.put(key, newNode);
        }
        else {
            oldNode=poolMap.get(key);
            lastSize=oldNode.calculateActualNodeSize();

            isUpdate=true;

            poolMap.put(key,newNode);
        }

        alertAddAndUpdateNodeToPoolChannel(oldNode,newNode,isUpdate);

        return newNode.calculateActualNodeSize()-lastSize;
    }

    @Override
    public Node getItem(String key){
        return poolMap.get(key);
    }

    @Override
    public void replaceNode(Node newNode, Node oldNode) {
        poolMap.replace(oldNode.getKey(),newNode);
    }

    @Override
    public Node getAndDeleteItem(String key) {
        return poolMap.remove(key);
    }

    @Override
    public void deleteItem(String key) {
        poolMap.remove(key);
    }

    @Override
    public Vector<Node> findKeys(String regexKey, int topItem) {
        Vector<Node> nodeVector=new Vector<>();
        for (Node node:poolMap.values()
             ) {
            if(topItem ==0) break;
            if(node.getKey().matches(regexKey)) {
                topItem--;
                nodeVector.add(node);
            }
        }

        return nodeVector;
    }

    @Override
    public Vector<Node> findValues(String regexValue, int topItem) {
        Vector<Node> nodeVector=new Vector<>();
        for (Node node:poolMap.values()
        ) {
            if(topItem ==0) break;
            if(node.deserializeNode().matches(regexValue)) {
                topItem--;
                nodeVector.add(node);
            }
        }

        return nodeVector;
    }

    @Override
    public void flushPool() {
        poolMap.clear();
    }

    @Override
    public void forceClearOnGroupOfItems(int numberElementToClear, int cutMaxTimeToLive) {
        for(Node node:poolMap.values()){
            if(numberElementToClear==0) break;
            else if(node.getTimeToLiveCounter() >= cutMaxTimeToLive && node.getTtl() >= 0){
                node.cutNode();
                numberElementToClear--;
            }
        }
    }

    @Override
    public long size() {
        return poolMap.size();
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer action) {
        poolMap.forEachValue(10L, (Consumer<? super Node>) action);
    }

    @Override
    public Spliterator spliterator() {
        return super.spliterator();
    }
}
