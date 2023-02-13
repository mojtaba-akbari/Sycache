package org.SyCache.CacheHelperModels;

public class CachePipelineMicroActsException extends Exception {
    String microCausedException;


    public CachePipelineMicroActsException(String message, String microCausedException) {
        super(message);
        this.microCausedException = microCausedException;
    }

    public String getMicroCausedException() {
        return microCausedException;
    }

    public void setMicroCausedException(String microCausedException) {
        this.microCausedException = microCausedException;
    }
}
