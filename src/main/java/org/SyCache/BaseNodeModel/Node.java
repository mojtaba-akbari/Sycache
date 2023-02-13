package org.SyCache.BaseNodeModel;

import org.SyCache.BasePoolModel.Pool;
import org.SyCache.CacheHelperModels.CacheMetadata;
import org.SyCache.CacheHelperModels.CacheMetadataEnum;
import org.SyCache.CacheHelperModels.CacheTag;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;

public abstract  class Node implements Runnable, Cloneable{
    protected int fixTotalSize;
    protected short ttl;
    protected short valueSize;

    protected short predictHit;

    protected String key;
    protected CacheTag tag;
    private int timeToLiveCounter;
    private String lastNodeError;
    private final Vector<Node> graphNode;

    private final Vector<CacheMetadata> cacheMetadata;
    private Node prevHistoryNodeLink;
    private Node nexHistoryNodeLink;
    private Pool poolPTR;

    private ScheduledFuture scheduledFuture;
    private int counterFrequenciesUsed;
    private long counterLastUsed;

    private  boolean isNodeEvict;
    private boolean isNodeDestroy;
    private boolean isNodeSecondChance;

    private final Long _LAST_USED_HIGH_WINDOW_=(long)(1<<30); // 15 min last //
    private final Integer _LAST_USED_HIGH_WINDOW_WITH_F_METRIC=(1<<14); // 7 min

    private final Short _TICK_EVICTED_CHECK_=10; // 30 sec

    public Node(String key, String value, CacheTag tag, short valueSize, short ttl, short predictHit, Vector<CacheMetadata> metadata, Pool poolPTR) {
        this.key=key;
        this.ttl = ttl;
        this.valueSize=valueSize;
        this.predictHit=predictHit;
        this.poolPTR=poolPTR;
        this.timeToLiveCounter =1;
        this.lastNodeError="";
        this.prevHistoryNodeLink=null;
        this.nexHistoryNodeLink=null;
        this.graphNode=new Vector<>();
        this.cacheMetadata=metadata;
        this.counterLastUsed = 1L;
        this.counterFrequenciesUsed=1;
        this.isNodeDestroy =false;
        this.isNodeEvict=false;
        this.isNodeSecondChance=false;

        if(tag != null) {
            this.tag = tag;
            this.tag.NodePTRHolder().addElement(this);
        }

        this.fixTotalSize=12;

        serializeNode(value);
    }

    private void nodeSchedulingTask() {
        synchronized (this) {
            if (!isNodeDestroy) {

                if (ttl > 0) {
                    if (timeToLiveCounter > ttl) {
                        selfDestructNode();
                    }
                }

                if(!isNodeEvict && ttl >= 0) {
                    if ((timeToLiveCounter % _TICK_EVICTED_CHECK_) == 0 && timeToLiveCounter != 0) {
                        if ((counterLastUsed >= _LAST_USED_HIGH_WINDOW_)
                                        || ((counterFrequenciesUsed / timeToLiveCounter < 1) && (counterLastUsed >= _LAST_USED_HIGH_WINDOW_WITH_F_METRIC))) {

                            poolPTR.addItemEvictedNodes(this);
                            isNodeEvict = true;
                        }

                        counterLastUsed = counterLastUsed << 1;
                    }
                }


                timeToLiveCounter++;
            } else {
                selfDestructNode();
            }
        }
    }

    public abstract void destructSerializedData();
    public abstract  void  serializeNode(String value);
    public abstract String deserializeNode();
    public abstract void updateNode(String value);
    public abstract int calculateActualNodeSize();


    private void selfDestructNode(){
        destructSerializedData();

        if (tag != null) {
            tag.NodePTRHolder().remove(this);
            tag = null;
        }

        poolPTR.deleteItem(key);

        poolPTR.delItemEvictedNodes(this);

        poolPTR = null;

        scheduledFuture.cancel(true);
    }

    public void commitNodeToNewState(Node newNode){
        synchronized (this){
            scheduledFuture.cancel(true);
            poolPTR.commitItem(newNode,this);
        }
    }

    public void cutNode(){
        synchronized (this) {
            isNodeDestroy =true;
        }
    }

    public void freeNodeFromEvicted(){
        synchronized (this){
            isNodeSecondChance = false;
            poolPTR.getEvictedNodes().remove(this);
            isNodeEvict=false;
            counterLastUsed = 1L;
            timeToLiveCounter = 0;
        }
    }
    public boolean takeAnotherChance(){
        synchronized (this) {
            if (isNodeSecondChance)
                return false;
            else {
                isNodeSecondChance = true;
                poolPTR.getEvictedNodes().remove(this);
                isNodeEvict=false;
                counterLastUsed = 1L;
                timeToLiveCounter = 0;
                return true;
            }
        }
    }

    public void accessNodeActions(){
        synchronized (this) {
            poolPTR.getEvictedNodes().remove(this);
            isNodeEvict=false;
            isNodeSecondChance=false;
            counterLastUsed = counterLastUsed == 0 ? 1 :
                    (counterLastUsed == 1 ? 1 : counterLastUsed >> 1);
            counterFrequenciesUsed++;
        }
    }

    public boolean isNodeAccessOnRegularPredict(){
        return predictHit != 0 && (timeToLiveCounter / predictHit <= counterFrequenciesUsed);
    }

    public Vector<CacheMetadata> getCacheMetadata() {
        return cacheMetadata;
    }

    public short getTtl() {
        return ttl;
    }

    public int getValueSize() {
        return calculateActualNodeSize();
    }

    public String getKey() {
        return key;
    }

    public CacheTag getTag(){
        return tag;
    }

    public Integer getTimeToLiveCounter() {
        return timeToLiveCounter;
    }

    public short getPredictHit() {
        return predictHit;
    }

    public void setLastNodeError(String lastNodeError) {
        this.lastNodeError = lastNodeError;
    }

    public void setPrevHistoryNodeLink(Node prevHistoryNodeLink) {
        this.prevHistoryNodeLink = prevHistoryNodeLink;
    }

    public void setNexHistoryNodeLink(Node nexHistoryNodeLink) {
        this.nexHistoryNodeLink = nexHistoryNodeLink;
    }

    public Node getPrevHistoryNodeLink() {
        return prevHistoryNodeLink;
    }

    public Node returnNexHistoryNodeLink() {
        return nexHistoryNodeLink;
    }

    public Vector<Node> getGraphNode() {
        return graphNode;
    }

    public String getLastNodeError() {
        return lastNodeError;
    }

    public void addNodeErrorMessage(String msg){
        lastNodeError=msg;
    }

    public ScheduledFuture returnScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    @Override
    public void run(){
        nodeSchedulingTask();
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
