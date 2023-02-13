package org.SyCache.CacheEntity;

import org.SyCache.CacheEntity.CacheInlineFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CacheInlineFunctionRepository extends JpaRepository <CacheInlineFunction, Integer>{
    CacheInlineFunction findCacheStreamByName(String name);
}
