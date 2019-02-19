package com.gamesparks.sdk.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gamesparks.sdk.Assert;
import com.gamesparks.sdk.AutoResetEvent;
import com.gamesparks.sdk.GSExtended;
import com.gamesparks.sdk.GSHelperMethods;

public class TestInstance {

	private static boolean mFinished;
	private static int mCounter;
	private static GSExtended mCurrInstance;
	
	@Test
	public void stress() throws Exception {
		mFinished = false;
		
		System.out.println("*** Stress ***");
		
		mCounter = 0;
		
		createInstance();
		
		waitFinishedTest();
	}
	
	private static void createInstance() throws Exception {
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("Stress" + mCounter, new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						mCounter ++;
						System.out.println(mCounter);
						if (mCounter >= 50) {
							System.out.println("Passed!");
							
							mFinished = true;
						} else {
							createInstance();
						}
					}
				});
			}
		});
	}

	private static void waitFinishedTest() throws Exception {
		long start = System.currentTimeMillis();
		
		while (System.currentTimeMillis() < start + GSHelperMethods.REQUEST_TIMEOUT * 2000) {
			if (mFinished || Assert.exc != null) {
				if (Assert.exc != null) {
					throw Assert.exc;
				}
				
				break;
			} else {
				Thread.sleep(100);
			}
		}
		
		assertTrue("Test failed!", mFinished);
	}
}
