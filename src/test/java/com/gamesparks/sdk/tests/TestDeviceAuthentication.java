package com.gamesparks.sdk.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gamesparks.sdk.Assert;
import com.gamesparks.sdk.GSExtended;
import com.gamesparks.sdk.GSHelperMethods;

public class TestDeviceAuthentication {

	private static boolean mFinished;
	
	private GSExtended mCurrInstance;
	private String token;
	private String token2;
	private String playerId;
	
	@Test
	public void authToken() throws Exception {
		mFinished = false;
		
		System.out.println("*** AuthToken ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("AuthToken", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						token = mCurrInstance.getGSPlatform().getAuthToken();
						
						assertNotNull(token);
						assertTrue(token.length() > 0);
						
						GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
							@Override
							public void function() throws Exception {
								mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("AuthToken", new GSHelperMethods.Callback() {
									@Override
									public void function() throws Exception {
										token2 = mCurrInstance.getGSPlatform().getAuthToken();
										
										assertNotNull(token2);
										assertTrue(token2.length() > 0);									
										assertFalse(token.equals(token2));

										GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
											@Override
											public void function() throws Exception {
												GSHelperMethods.shutDownAllInstances();
												
												System.out.println("Passed!");
												
												mFinished = true;
											}
										});
									}
								});
							}
						});
					}
				});
			}
		});
		
		waitFinishedTest();
	}
	
	@Test
	public void userIdOnAuthentication() throws Exception {
		mFinished = false;
		
		System.out.println("*** UserIdOnAuthentication ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("UserIdOnAuthentication", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						playerId = mCurrInstance.getGSPlatform().getPlayerId();
						
						GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
							@Override
							public void function() throws Exception {
								mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("UserIdOnAuthentication", new GSHelperMethods.Callback() {
									@Override
									public void function() throws Exception {
										assertEquals(playerId, mCurrInstance.getGSPlatform().getPlayerId());

										GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
											@Override
											public void function() throws Exception {
												GSHelperMethods.shutDownAllInstances();
												
												System.out.println("Passed!");
												
												mFinished = true;
											}
										});
									}
								});
							}
						});
					}
				});
			}
		});
		
		waitFinishedTest();
	}
	
	@Test
	public void receiveUserIdOnAuthentication() throws Exception {
		mFinished = false;
		
		System.out.println("*** ReceiveUserIdOnAuthentication ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("ReceiveUserIdOnAuthentication", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						playerId = mCurrInstance.getGSPlatform().getPlayerId();
						
						assertNotNull(playerId);
						assertTrue(playerId.length() > 0);
						
						GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
							@Override
							public void function() throws Exception {
								GSHelperMethods.shutDownAllInstances();
								
								System.out.println("Passed!");
								
								mFinished = true;
							}
						});
					}
				});
			}
		});
		
		waitFinishedTest();
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
