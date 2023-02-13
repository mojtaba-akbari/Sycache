package org.SyCache.CommandModels;

import org.SyCache.CacheHelperModels.*;
import org.SyCache.Controllers.InterfaceBaseCommandModel;
import org.SyCache.Controllers.Cache;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

@CacheCommandAnnotation(commandName = "SET",commandPattern = "KEY VALUE TAG SIZE TTL PREDICT METADATA",commandDescription = "Set Value Of Key From Pool Model",singleCommand = "false")
public class SetItemCommand extends CacheCommand {

    public SetItemCommand(Cache cache) {
        super(cache);
        processPattern(this);
    }

    @Override
    public CacheResult execute(InterfaceBaseCommandModel command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if(!cache.getStorage().existsPool(command.getPoolName()))
            return new CacheResult(null,ResultEnum.NUL);

        int upperIndex=(pattern.length-1);
        for(int i=0;i<command.getTailCommand().length - upperIndex;i+= pattern.length){
            String key=command.getTailCommand()[i];
            String value=command.getTailCommand()[i+1];
            String cacheTag=command.getTailCommand()[i+2];
            short valueSize= Short.parseShort(command.getTailCommand()[i+3]);
            short ttl=Short.parseShort(command.getTailCommand()[i+4]);
            short predictHit=Short.parseShort(command.getTailCommand()[i+5]);

            String[] metadataParts=command.getTailCommand()[i+6].split(",");
            Vector<CacheMetadata> cacheMetadataVector=new Vector<>(metadataParts.length);

            for (String metadata:metadataParts) {
                String[] meta=metadata.split("=");
                cacheMetadataVector.add(new CacheMetadata(CacheMetadataEnum.valueOf(meta[0].toUpperCase()),meta[1]));
            }

            int newSize;
            CacheTag tag=null;

            // TAG
            if(!cacheTag.isEmpty())
            {
                tag=cache.getStorage().getPool(command.getPoolName()).getTagValidateStorage().get(cacheTag);
                if(tag != null)
                    tag.setValidate(true);
                else {
                    tag=new CacheTag(cacheTag);
                    cache.getStorage().getPool(command.getPoolName()).getTagValidateStorage().put(cacheTag,tag);
                }
            }

            newSize=cache.getStorage().getPool(command.getPoolName()).addItem(key,value,tag,valueSize,ttl,predictHit,cacheMetadataVector);


            cache.addNodeTaskToCacheSchedulerExecutor(cache.getStorage().getPool(command.getPoolName()).getItem(key));

            cache.calculateSize(newSize);
        }

        return new CacheResult(null,ResultEnum.ACK);
    }
}
