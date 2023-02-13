package org.SyCache.DataNodeModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.BasePoolModel.Pool;
import org.SyCache.CacheHelperModels.CacheMetadata;
import org.SyCache.CacheHelperModels.CacheTag;

import java.util.Vector;

public class StringNode extends Node {

    String value;

    public StringNode(String key, String value, CacheTag tag, short valueSize, short ttl, short predictHit, Vector<CacheMetadata> metadata, Pool pool) {
        super(key, value, tag, valueSize, ttl, predictHit, metadata, pool);

        this.fixTotalSize=0;
    }

    public StringNode(String key,String value){
        super (key,value, (CacheTag) null, (short) 0, (short) 0, (short) 0,null,null);
        this.fixTotalSize=0;
    }

    public String getValue() {
        return deserializeNode();
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void destructSerializedData() {
        value=null;
    }

    @Override
    public void serializeNode(String value) {
        this.value=value;
    }

    @Override
    public String deserializeNode() {
        return value;
    }

    @Override
    public void updateNode(String value) {
        synchronized (value){
            this.value=value;
        }
    }

    @Override
    public int calculateActualNodeSize() {
        return fixTotalSize+value.length()*2;
    }
}
