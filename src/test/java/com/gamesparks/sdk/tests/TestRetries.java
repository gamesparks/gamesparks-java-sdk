package com.gamesparks.sdk.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gamesparks.sdk.Assert;
import com.gamesparks.sdk.GSExtended;
import com.gamesparks.sdk.GSHelperMethods;

public class TestRetries {

	private static boolean mFinished;
	
	private GSExtended mCurrInstance;
	
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
