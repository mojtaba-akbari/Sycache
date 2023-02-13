package org.SyCache.CacheHelperModels;

import org.SyCache.BaseNodeModel.Node;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

public class CacheResult implements Iterator<Node> {

    ResultEnum state;
    Vector<Node>  holder;

    int indexIterator=-1;

    public CacheResult(Vector<Node> holder, ResultEnum state) {
        this.holder = holder;
        this.state= state;
    }

    public ResultEnum getState(){
        return state;
    }

    public Vector<Node> getHolder() {
        return holder;
    }

    @Override
    public boolean hasNext() {
        if(holder == null) {
            return false;
        }
        else if(indexIterator < holder.size()-1)
            return true;
        else{
            indexIterator=-1;
            return false;
        }
    }

    @Override
    public Node next() {
        indexIterator++;
        return holder.get(indexIterator);
    }

    @Override
    public void remove() {
        Iterator.super.remove();
    }

}
