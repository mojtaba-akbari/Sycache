package org.SyCache.CacheHelperModels;

import org.SyCache.BaseNodeModel.Node;
import java.util.Vector;

public class CacheTag {
    String tag;

    Boolean isValidate=true;
    Vector<Node> nodePTRHolder=new Vector<>();

    public CacheTag(String tag){
        this.tag=tag;
    }

    public String getTag() {
        return tag;
    }

    public Boolean getValidate() {
        return isValidate;
    }

    public void setValidate(Boolean validate) {
        isValidate = validate;
    }

    public Vector<Node> NodePTRHolder() {
        return nodePTRHolder;
    }
}
