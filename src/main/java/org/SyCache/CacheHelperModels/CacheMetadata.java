package org.SyCache.CacheHelperModels;

public class CacheMetadata {
    private CacheMetadataEnum cacheMetadataEnum;
    private String value;

    public CacheMetadata(CacheMetadataEnum cacheMetadataEnum, String value) {
        this.cacheMetadataEnum = cacheMetadataEnum;
        this.value = value;
    }

    public CacheMetadataEnum getCacheMetadataEnum() {
        return cacheMetadataEnum;
    }

    public void setCacheMetadataEnum(CacheMetadataEnum cacheMetadataEnum) {
        this.cacheMetadataEnum = cacheMetadataEnum;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
