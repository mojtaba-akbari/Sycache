package org.SyCache.CacheHelperModels;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.SyCache.BaseNodeModel.NodeStateEnum;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

public class CacheOutputDriver {
    private final String propertiesFile="/application.properties";
    private CacheChannelNodeStateHolder cacheChannelNodeStateHolder;
    private final CacheOutputTypeDriverEnum outputEnum;
    private JedisPool jedisPools;

    private Connection connectionMysql;
    private final Properties properties;
    public CacheOutputDriver(CacheOutputTypeDriverEnum outputEnum) throws IOException, SQLException, ClassNotFoundException {
        this.outputEnum=outputEnum;

        Resource resource=new ClassPathResource(propertiesFile);
        properties= PropertiesLoaderUtils.loadProperties(resource);

        // Call Factory For This Type //
        switch (outputEnum){
            case REDIS -> {
                redisFactory();
            }
            case MYSQL->{
                mysqlFactory();
            }
        }
    }

    public void broadcast() throws SQLException, IOException {
        switch (outputEnum){
            case STDOUT -> cliOutPut();
            case REDIS -> redisPut();
            case MYSQL -> mysqlPut();
        }

        this.cacheChannelNodeStateHolder =null;
    }

    public CacheChannelNodeStateHolder getCacheChannelNodeStateHolder() {
        return cacheChannelNodeStateHolder;
    }

    public void setCacheChannelNodeStateHolder(CacheChannelNodeStateHolder cacheChannelNodeStateHolder) {
        this.cacheChannelNodeStateHolder = cacheChannelNodeStateHolder;
    }

    private void cliOutPut(){
        System.out.println(cacheChannelNodeStateHolder.getNewNode().getKey()+" "+ cacheChannelNodeStateHolder.getNewNode().deserializeNode());
    }

    private void mysqlPut() throws IOException, SQLException {
        ObjectMapper objectMapper=new ObjectMapper();
        PreparedStatement preparedStatement = null;

        String dbTable="";
        String uniqueIdColumn="";
        String uniqueIdColumnValue="";

        String fieldsAndValue="";

        for (CacheMetadata mysqlMetaData: cacheChannelNodeStateHolder.getNewNode().getCacheMetadata()) {
            if(mysqlMetaData.getCacheMetadataEnum().equals(CacheMetadataEnum.DBTABLE))
                dbTable=mysqlMetaData.getValue();
            else if (mysqlMetaData.getCacheMetadataEnum().equals(CacheMetadataEnum.ENTITYID))
                uniqueIdColumn=mysqlMetaData.getValue();
        }

        JsonNode jsonNode=objectMapper.readTree(cacheChannelNodeStateHolder.getNewNode().deserializeNode());

        if(cacheChannelNodeStateHolder.nodeStateEnum.equals(NodeStateEnum.INSERT)) {
            for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();

                fieldsAndValue+=jsonNode.get(fieldName).asText()+",";
            }

            fieldsAndValue=fieldsAndValue.substring(0,fieldsAndValue.length()-1);

            preparedStatement = connectionMysql.prepareStatement("INSERT INTO " + dbTable + " values(" + fieldsAndValue + ")");
        }
        else if (cacheChannelNodeStateHolder.nodeStateEnum.equals(NodeStateEnum.UPDATE)){

            JsonNode jsonOldNode=objectMapper.readTree(cacheChannelNodeStateHolder.getOldNode().deserializeNode());

            for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                fieldsAndValue+=fieldName+"="+jsonNode.get(fieldName).asText()+",";

                if(uniqueIdColumn.equals(fieldName)) // Do not fetch unique id column //
                    uniqueIdColumnValue=jsonOldNode.get(fieldName).asText();
            }

            fieldsAndValue=fieldsAndValue.substring(0,fieldsAndValue.length()-1);

            preparedStatement = connectionMysql.prepareStatement("UPDATE " + dbTable + " SET " + fieldsAndValue + " WHERE ("+uniqueIdColumn+"="+uniqueIdColumnValue+")");
        }



        preparedStatement.execute();
    }

    private void mysqlFactory() throws SQLException, ClassNotFoundException {
        connectionMysql= DriverManager.getConnection(properties.getProperty("mysql.uri"),properties.getProperty("mysql.user"),properties.getProperty("mysql.pass"));
    }

    private void redisPut(){
        try(Jedis jedis= jedisPools.getResource()){
            jedis.set(cacheChannelNodeStateHolder.getNewNode().getKey(), cacheChannelNodeStateHolder.getNewNode().deserializeNode());
        }
    }

    private void redisFactory(){

        buildRedisPoolConfig();

        jedisPools =new JedisPool(buildRedisPoolConfig(),properties.getProperty("redis.uri"));
    }

    private JedisPoolConfig buildRedisPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        return poolConfig;
    }
}

