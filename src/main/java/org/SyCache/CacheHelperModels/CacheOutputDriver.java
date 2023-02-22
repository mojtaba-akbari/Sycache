package org.SyCache.CacheHelperModels;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionImpl;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.util.AwsHostNameUtils;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.SyCache.BaseNodeModel.NodeStateEnum;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.ByteArrayInputStream;
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
    private ObjectMapper mysqlObjectMapper;

    private AmazonS3 s3client;
    private TransferManager transferManager;
    private AWSCredentials credentials;

    private Properties kafkaProperties;
    private KafkaProducer<String,String> kafkaProducer;

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
            case S3 -> {
                s3Factory();
            }
            case KAFKA -> {
                kafkaFactory();
            }
        }
    }

    public void broadcast() throws SQLException, IOException {
        switch (outputEnum){
            case STDOUT -> cliOutPut();
            case REDIS -> redisPut();
            case MYSQL -> mysqlPut();
            case S3-> s3Put();
            case KAFKA -> kafkaPut();
        }

        this.cacheChannelNodeStateHolder = null;
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

    private void redisPut(){
        try(Jedis jedis= jedisPools.getResource()){
            jedis.set(cacheChannelNodeStateHolder.getNewNode().getKey(), cacheChannelNodeStateHolder.getNewNode().deserializeNode());
        }
    }

    private void s3Put(){

        String bucketName="";
        String fileExt="";

        for (CacheMetadata mysqlMetaData: cacheChannelNodeStateHolder.getNewNode().getCacheMetadata()) {
            if(mysqlMetaData.getCacheMetadataEnum().equals(CacheMetadataEnum.BUCKETNAME))
                bucketName=mysqlMetaData.getValue();
            else if (mysqlMetaData.getCacheMetadataEnum().equals(CacheMetadataEnum.FILEEXTENSION))
                fileExt=mysqlMetaData.getValue();
        }

        ByteArrayInputStream input = new ByteArrayInputStream(cacheChannelNodeStateHolder.getNewNode().deserializeNode().getBytes());
        ObjectMetadata objectMetadata=new ObjectMetadata();
        objectMetadata.setContentLength(input.available());

        try {
            transferManager.upload(bucketName, cacheChannelNodeStateHolder.getNewNode().getKey() + "." + fileExt, input, objectMetadata);
        }
        finally {
            IOUtils.closeQuietly(input,null);
        }
    }

    private void kafkaPut(){
        String topicName="";

        for (CacheMetadata mysqlMetaData: cacheChannelNodeStateHolder.getNewNode().getCacheMetadata()) {
            if(mysqlMetaData.getCacheMetadataEnum().equals(CacheMetadataEnum.TOPICNAME))
                topicName=mysqlMetaData.getValue();
        }

        ProducerRecord<String,String> producerRecord=new ProducerRecord<>(topicName,cacheChannelNodeStateHolder.getNewNode().getKey(),cacheChannelNodeStateHolder.getNewNode().deserializeNode());

        kafkaProducer.send(producerRecord);
        kafkaProducer.flush();

    }

    private void mysqlPut() throws IOException, SQLException {
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

        JsonNode jsonNode= mysqlObjectMapper.readTree(cacheChannelNodeStateHolder.getNewNode().deserializeNode());

        if(cacheChannelNodeStateHolder.nodeStateEnum.equals(NodeStateEnum.INSERT)) {
            for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();

                fieldsAndValue+=jsonNode.get(fieldName).asText()+",";
            }

            fieldsAndValue=fieldsAndValue.substring(0,fieldsAndValue.length()-1);

            preparedStatement = connectionMysql.prepareStatement("INSERT INTO " + dbTable + " values(" + fieldsAndValue + ")");
        }
        else if (cacheChannelNodeStateHolder.nodeStateEnum.equals(NodeStateEnum.UPDATE)){

            JsonNode jsonOldNode= mysqlObjectMapper.readTree(cacheChannelNodeStateHolder.getOldNode().deserializeNode());

            for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                fieldsAndValue+=fieldName+"="+jsonNode.get(fieldName).asText()+",";

                if(uniqueIdColumn.equals(fieldName)) // Do not fetch unique id column //
                    uniqueIdColumnValue=jsonOldNode.get(fieldName).asText();
            }

            fieldsAndValue=fieldsAndValue.substring(0,fieldsAndValue.length()-1);

            preparedStatement = connectionMysql.prepareStatement("UPDATE " + dbTable + " SET " + fieldsAndValue + " WHERE ("+uniqueIdColumn+"="+uniqueIdColumnValue+")");
        }


        assert preparedStatement != null;

        preparedStatement.execute();
    }

    private void s3Factory(){
        credentials = new BasicAWSCredentials(
                properties.getProperty("s3.accessKey"),
                properties.getProperty("s3.secretKey")
        );

        s3client  = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                properties.getProperty("s3.host"),
                                AwsHostNameUtils.parseRegion(properties.getProperty("s3.host"), AmazonS3Client.S3_SERVICE_NAME)
                        )
                )
                .withPathStyleAccessEnabled(true)
                .build();

        transferManager= TransferManagerBuilder.standard()
                .withS3Client(s3client)
                .build();
    }

    private void mysqlFactory() throws SQLException {
        mysqlObjectMapper =new ObjectMapper();
        connectionMysql= DriverManager.getConnection(properties.getProperty("mysql.uri"),properties.getProperty("mysql.user"),properties.getProperty("mysql.pass"));
    }

    private void kafkaFactory(){
        kafkaProperties=new Properties();
        kafkaProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,properties.getProperty("kafka.bootstrap"));
        kafkaProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        kafkaProducer=new KafkaProducer<>(kafkaProperties);
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

