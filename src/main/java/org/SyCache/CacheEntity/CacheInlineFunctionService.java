package org.SyCache.CacheEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CacheInlineFunctionService {
    @Autowired
    CacheInlineFunctionRepository cacheInlineFunctionRepository;

    public List getAllCacheStream() {
        List cacheStorage = new ArrayList();
        cacheInlineFunctionRepository.findAll().forEach(cacheInlineFunction -> cacheStorage.add(cacheInlineFunction));
        return cacheStorage;
    }

    public CacheInlineFunction getCacheStreamById(int id) {
        return cacheInlineFunctionRepository.findById(id).get();
    }

    public CacheInlineFunction getCacheStreamByName(String name) {
        return cacheInlineFunctionRepository.findCacheStreamByName(name);
    }

    public void saveOrUpdate(CacheInlineFunction cacheInlineFunction) {
        cacheInlineFunctionRepository.save(cacheInlineFunction);
    }

    public void delete(int id) {
        cacheInlineFunctionRepository.deleteById(id);
    }
}
