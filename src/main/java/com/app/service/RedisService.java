package com.app.service;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service("redisService")  
public class RedisService {  
  
    private Logger logger = LoggerFactory.getLogger("RedisService");  
  
    @Autowired  
    RedisTemplate<?, ?> redisTemplate;  
  
    // 线程池  
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(  
            256, 256, 30L, TimeUnit.SECONDS,  
            new LinkedBlockingQueue<Runnable>(),  
            new BasicThreadFactory.Builder().daemon(true)  
                    .namingPattern("redis-oper-%d").build(),  
            new ThreadPoolExecutor.CallerRunsPolicy());  
  
    public void set(final String key, final String value) {  
        redisTemplate.execute(new RedisCallback<Object>() {  
            public Object doInRedis(RedisConnection connection)  
                    throws DataAccessException {  
                connection.set(  
                        redisTemplate.getStringSerializer().serialize(key),  
                        redisTemplate.getStringSerializer().serialize(value));  
                logger.debug("save key:" + key + ",value:" + value);  
                return null;  
            }  
        });  
    }  
  
    public String get(final String key) {  
        return redisTemplate.execute(new RedisCallback<String>() {  
            public String doInRedis(RedisConnection connection)  
                    throws DataAccessException {  
                byte[] byteKye = redisTemplate.getStringSerializer().serialize(  
                        key);  
                if (connection.exists(byteKye)) {  
                    byte[] byteValue = connection.get(byteKye);  
                    String value = redisTemplate.getStringSerializer()  
                            .deserialize(byteValue);  
                    logger.debug("get key:" + key + ",value:" + value);  
                    return value;  
                }  
                logger.error("valus does not exist!,key:"+key);  
                return null;  
            }  
        });  
    }  
  
    public void delete(final String key) {  
        redisTemplate.execute(new RedisCallback<Object>() {  
            public Object doInRedis(RedisConnection connection) {  
                connection.del(redisTemplate.getStringSerializer().serialize(  
                        key));  
                return null;  
            }  
        });  
    }  
  
    /** 
     * 线程池并发操作redis 
     *  
     * @param keyvalue 
     */  
    public void mulitThreadSaveAndFind(final String keyvalue) {  
        executor.execute(new Runnable() {  
            public void run() {  
                try {  
                    set(keyvalue, keyvalue);  
                    String value=get(keyvalue); 
                    logger.debug(value);
                } catch (Throwable th) {  
                    // 防御性容错，避免高并发下的一些问题  
                    logger.error("", th);  
                }  
            }  
        });  
    } 
    
    public void add2List(final String key, final String value){
        redisTemplate.execute(new RedisCallback<Object>() {  
            public Object doInRedis(RedisConnection connection)  
                    throws DataAccessException {  
                connection.lPush(redisTemplate.getStringSerializer().serialize(key),  
                        		 redisTemplate.getStringSerializer().serialize(value));
                logger.debug("save value:" + value + " to list:" + key); 
                return null;  
            }  
        });  
    }
    
	public List<Object> getMultiFromList(final String key, final int batchSize) {
		List<Object> results = redisTemplate.executePipelined(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
				for (int i = 0; i < batchSize; i++) {
					stringRedisConn.rPop(key);
				}
				return null;
			}
		});
		return results;
	}
    
}  
