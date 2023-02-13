package org.SyCache.DataNodeModels;

import org.SyCache.BaseNodeModel.Node;
import org.SyCache.BasePoolModel.Pool;
import org.SyCache.CacheHelperModels.CacheMetadata;
import org.SyCache.CacheHelperModels.CacheTag;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class ByteNode extends Node {

    public ByteBuffer data;

    public ByteNode(String key, String value, CacheTag tag, short valueSize, short ttl, short predictHit, Vector<CacheMetadata> metadata, Pool pool) {
        super(key, value, tag, valueSize, ttl,predictHit,metadata, pool);
    }

    public String getData() {
        return deserializeNode();
    }

    @Override
    public int calculateActualNodeSize() {
        return (fixTotalSize+data.capacity()+key.length()+(tag != null?tag.getTag().length():0));
    }

    @Override
    public void destructSerializedData() {
        data=null;
    }

    @Override
    public void updateNode(String value) {
        synchronized (this){
            data.clear();
            serializeNode(value);
        }
    }

    @Override
    public void serializeNode(String value){
        if (valueSize != 0){
            data = ByteBuffer.allocate(valueSize);
            switch (valueSize){
                case 1:
                    data.putChar(value.charAt(0));
                    break;
                case 2:
                    data.putShort(Short.valueOf(value,10));
                    break;
                case 4:
                    data.putInt(Integer.valueOf(value,10));
                    break;
                case 8:
                    data.putLong(Long.valueOf(value,10));
                    break;
            }
        }
        else {
            data = ByteBuffer.allocate(value.length());
            data.put(value.getBytes(StandardCharsets.UTF_8));
        }

        fixTotalSize+=data.capacity();
    }

    @Override
    public String deserializeNode() {
        data.rewind();

        switch (valueSize) {
            case 1:
                return String.valueOf(data.getChar());
            case 2:
                return String.valueOf(data.getShort());
            case 4:
                return String.valueOf(data.getInt());
            case 8:
                return String.valueOf(data.getLong());
            default:
                return new String(data.array());
        }
    }

}
