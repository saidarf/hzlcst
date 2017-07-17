package com.hzl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HzlcstApplicationTests {

	private static final Logger logger = Logger.getLogger(HzlDcApplication.class);

	@Test
	public void testHzlDcStart(){
		int instCount = 10;// Change this to change number of instance it's waiting for before write "We are ready"
		
		ExecutorService execSvc = Executors.newFixedThreadPool(instCount);
		List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>();
        for (int i = 0; i < instCount ; i++) {
            callables.add(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return HzlDcApplication.readWriteToCache();
                }
            });
        }
        
        int count=0;
        try {
            List<Future<Boolean>> execResultSet = execSvc.invokeAll(callables, 10, TimeUnit.MINUTES);
            for (int i = 0; i < execResultSet.size(); i++) {
                if (execResultSet.get(i).get() != null && execResultSet.get(i).get().booleanValue()==true) {
                	count++;
                }
            }
        } catch (InterruptedException e) {
            logger.debug(e.getMessage());
        } catch (ExecutionException e) {
            logger.debug(e.getMessage());
        }
        execSvc.shutdown();
        
        Assert.assertTrue("All " + instCount + " instances are ready", count==1);
	}
	
	@Test
	public void testHzlDcStartCoordinated(){
		int instCount = 10;// Change this to change number of instance it's waiting for before write "We are ready"
		
		ExecutorService execSvc = Executors.newFixedThreadPool(instCount);
		List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>();
        for (int i = 0; i < instCount ; i++) {
            callables.add(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return HzlDcApplication.readWriteToCache(instCount, 10);//instanceCount, Wait-timeout time in min
                }
            });
        }
        
        int count=0;
        try {
            List<Future<Boolean>> execResultSet = execSvc.invokeAll(callables, 10, TimeUnit.MINUTES);
            for (int i = 0; i < execResultSet.size(); i++) {
                if (execResultSet.get(i).get() != null && execResultSet.get(i).get().booleanValue()==true) {
                	count++;
                }
            }
        } catch (InterruptedException e) {
            logger.debug(e.getMessage());
        } catch (ExecutionException e) {
            logger.debug(e.getMessage());
        }
        execSvc.shutdown();
        
        Assert.assertTrue("All " + instCount + " instances are ready", count==1);
	}

}
