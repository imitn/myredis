package com.app.service;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RedisServiceTest {

	private RedisService redisService;  
	private ClassPathXmlApplicationContext context; 
	
	private final Logger logger = LoggerFactory.getLogger(RedisServiceTest.class);
	
    @Test  
    public void testSave() throws InterruptedException {  

    	System.setProperty("spring.profiles.active", "production_manual");
    	
    	String[] ctx = new String[] {"applicationContext-config.xml"};
    	
    	logger.debug("--------------------begin-----------------------");
    	context = new ClassPathXmlApplicationContext( ctx );  
    	logger.debug("--------------------end-----------------------");
    	
		redisService = (RedisService) context.getBean("redisService");  
		
		
		// a test case
/*		List<Object> alist=redisService.getMultiFromList("alist", 5);
		for(Object s:alist){
			logger.debug((String)s);
		}*/

    	// a test case
/*        int i = 1;  
        while (true) {  
            Thread.sleep(1);  
            try { 
            	redisService.mulitThreadSaveAndFind("" + i);
            	redisService.add2List("alist", "" + i);
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            i++;
            if(i==20){
            	break;
            }
        }  */
		
		
		
    }  
  
}
