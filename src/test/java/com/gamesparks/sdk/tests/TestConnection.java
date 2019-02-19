package com.gamesparks.sdk.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gamesparks.sdk.Assert;
import com.gamesparks.sdk.AutoResetEvent;
import com.gamesparks.sdk.GSEventConsumer;
import com.gamesparks.sdk.GSExtended;
import com.gamesparks.sdk.GSHelperMethods;
import com.gamesparks.sdk.api.autogen.GSRequestBuilder.LogEventRequest;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder.LogEventResponse;

public class TestConnection {

	private static boolean mFinished;
	
	private GSExtended mCurrInstance;
	private AutoResetEvent mEvent;
	private AutoResetEvent mResetOnError;
	private AutoResetEvent mResetOnReceive;

	@Test
	public void shutdown() throws Exception {
		mFinished = false;
		
		System.out.println("*** Shutdown ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("Shutdown", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
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
	
		waitFinishedTest();
	}
	
	@Test
	public void createAndShutdown() throws Exception {
		mFinished = false;
		
		System.out.println("*** CreateAndShutdown ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("CreateAndShutdown", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				mEvent = new AutoResetEvent(false);
				
				mCurrInstance.setOnAvailable(new GSEventConsumer<Boolean>() {
					@Override
					public void onEvent(Boolean available) {			
						if (!available) {
							mEvent.set();
						}
					}
				});
				
				GSHelperMethods.shutDown(mCurrInstance);
				
				Thread.sleep(2000);
				
				mEvent.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
					@Override
					public void function(boolean timedout) throws Exception {
						assertFalse("GS did not disconnect.", timedout);
							
						assertFalse("GS was still available.", mCurrInstance.isAvailable());
						
						assertTrue("There are still requests in the queue.", mCurrInstance.getRequestQueueCount() == 0);
						
						System.out.println("Passed!");
						
						mFinished = true;
					}
				});
			}
		});
		
		waitFinishedTest();
	}
	
	@Test
	public void createAndShutdown2() throws Exception {
		createAndShutdown();
		
		mFinished = false;
		
		System.out.println("*** CreateAndShutdown2 ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("CreateAndShutdown", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				mEvent = new AutoResetEvent(false);
				
				mCurrInstance.setOnAvailable(new GSEventConsumer<Boolean>() {
					@Override
					public void onEvent(Boolean available) {			
						if (!available) {
							mEvent.set();
						}
					}
				});
				
				GSHelperMethods.shutDown(mCurrInstance);
				
				Thread.sleep(2000);
				
				mEvent.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
					@Override
					public void function(boolean timedout) throws Exception {
						assertFalse("GS did not disconnect.", timedout);
							
						assertFalse("GS was still available.", mCurrInstance.isAvailable());
						
						assertTrue("There are still requests in the queue.", mCurrInstance.getRequestQueueCount() == 0);
						
						System.out.println("Passed!");
						
						mFinished = true;
					}
				});
			}
		});
		
		waitFinishedTest();
	}
	
	@Test
	public void shutdownWithNoNetwork() throws Exception {
		mFinished = false;
		
		System.out.println("*** ShutdownWithNoNetwork ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("ShutdownWithNoNetwork", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				mCurrInstance.setNetworkAvailable(false);
				
				GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						mCurrInstance.setNetworkAvailable(true);
						
						assertFalse(mCurrInstance.isAvailable());
						
						GSHelperMethods.shutDownAllInstances();
						
						System.out.println("Passed!");
						
						mFinished = true;
					}
				});
			}
		});
		
		waitFinishedTest();
	}
	
	@Test
	public void webSocketDisconnectReopen() throws Exception {
		mFinished = false;
		
		System.out.println("*** WebSocketDisconnectReopen ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("WebSocketDisconnectReopen", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				mCurrInstance.closeAllSockets();
				
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
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
	
	@Test
	public void webSocketErrorTimeout() throws Exception {
		mFinished = false;
		
		System.out.println("*** WebSocketErrorTimeout ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("WebSocketDisconnectReopen", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {	
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						mCurrInstance.setNetworkAvailable(false);
						mCurrInstance.closeAllSockets();
						
						LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
						mResetOnError = new AutoResetEvent(false);
						mResetOnReceive = new AutoResetEvent(false);
						
						request.send(new GSEventConsumer<LogEventResponse>() {
					        @Override
					        public void onEvent(LogEventResponse response) {
					        	if (response.hasErrors()) {
					        		mResetOnError.set();
					        	} else {
					        		mResetOnReceive.set();
					        	}					   
					        }
					    });
						
						mResetOnError.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
							@Override
							public void function(boolean timedout) throws Exception {
								assertFalse("Call never timed out.", timedout);
								
								mCurrInstance.setNetworkAvailable(true);
								
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
