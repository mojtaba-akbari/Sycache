package org.SyCache.CacheHelperModels;

import org.SyCache.BaseNodeModel.Node;

public interface CacheChannel {
    abstract void insertNode(Object obj);
    abstract void updateNode(Object oldObject,Object newObject);
    abstract void evictNode(Object obj);
    abstract void destroyNode(Object obj);
}
