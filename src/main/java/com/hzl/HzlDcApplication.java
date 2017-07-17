package com.hzl;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.DuplicateInstanceNameException;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IMap;

@SpringBootApplication
@EnableCaching
public class HzlDcApplication {
	private static final Logger logger = Logger.getLogger(HzlDcApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(HzlDcApplication.class, args);
		//readWriteToCache(10, 10); //number of instances & timeout min
		readWriteToCache();
	}
	
	/*
	 * This fn will initiate or obtain an existing instance 
	 * of Hazelcast and write a flag in to it to indicate  
	 * if a module instance has already written the message
	 * "We are Started"
	 */
	public static Boolean readWriteToCache(){
		HazelcastInstance hzlInstance;
		Boolean retVal = false;
		
		try{
		    hzlInstance = Hazelcast.newHazelcastInstance();
		} catch (DuplicateInstanceNameException ignored) {
			hzlInstance = Hazelcast.getHazelcastInstanceByName("hzInstance1");
	    }

		IMap<String, Boolean> map = hzlInstance.getMap("devCache");			
        Boolean bStrtd = map.get("is-started");
        if(null == bStrtd){
        	map.putIfAbsent("is-started", false);
        	if (map.tryLock("is-started")) {
        		bStrtd = map.get("is-started");
                if (!bStrtd) {
                    System.out.println("We are started!");
                    map.put("is-started", true);
                    retVal = true;
                }
                map.unlock("is-started");
        	}
        }
        return retVal;
		
	}

	/*
	 * This fn will initiate or obtain an existing instance 
	 * of Hazelcast and write to it once
	 * all the 10 instances are started and ready
	 */
	public static Boolean readWriteToCache(int instCount, int waitTimeMin){
		HazelcastInstance hzlInstance;
		Boolean retVal = false;
		
		try{
		    hzlInstance = Hazelcast.newHazelcastInstance();
		} catch (DuplicateInstanceNameException ignored) {
			hzlInstance = Hazelcast.getHazelcastInstanceByName("hzInstance1");
	    }
		
        ICountDownLatch latch = hzlInstance.getCountDownLatch( "countDownLatch" );
        
        boolean b = latch.trySetCount(instCount);
        latch.countDown();

        try {
			boolean waitOver = latch.await(waitTimeMin, TimeUnit.MINUTES );
			if(waitOver){
				IMap<String, Boolean> map = hzlInstance.getMap("devCache2");			
		        Boolean bStrtd = map.get("is-started");
		        if(null == bStrtd){
		        	map.putIfAbsent("is-started", false);
		        	if (map.tryLock("is-started")) {
		        		bStrtd = map.get("is-started");
		                if (!bStrtd) {
		                    System.out.println("We are started!");
		                    map.put("is-started", true);
		                    retVal = true;
		                }
		                map.unlock("is-started");
		        	}
		        }
			}
			latch.destroy();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return retVal;
		
	}

}
