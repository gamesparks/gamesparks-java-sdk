package com.gamesparks.sdk.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gamesparks.sdk.Assert;
import com.gamesparks.sdk.AutoResetEvent;
import com.gamesparks.sdk.GSEventConsumer;
import com.gamesparks.sdk.GSExtended;
import com.gamesparks.sdk.GSHelperMethods;
import com.gamesparks.sdk.api.autogen.GSRequestBuilder.LogEventRequest;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder.LogEventResponse;

public class TestSendRequests {

	private static boolean mFinished;
	
	private GSExtended mCurrInstance;
	private LogEventResponse mLogResponse = null;
	private LogEventResponse mLogResponse1 = null;
	private LogEventResponse mLogResponse2 = null;
	private LogEventResponse mLogErrorResponse = null;
	private AutoResetEvent mLogResponseEvent;
	private AutoResetEvent mLogResponseEvent1;
	private AutoResetEvent mLogResponseEvent2;
	
	@Test
	public void sendRequestThroughInstance() throws Exception {
		mFinished = false;
		
		System.out.println("*** SendRequestThroughInstance ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("SendRequestThroughInstance", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
						mLogResponseEvent = new AutoResetEvent(false);
						
						request.send(new GSEventConsumer<LogEventResponse>() {
					        @Override
					        public void onEvent(LogEventResponse response) {
					        	mLogResponse = response;
								mLogResponseEvent.set();
					        }
					    });
						
						mLogResponseEvent.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
							@Override
							public void function(boolean timedout) throws Exception {
								assertFalse("Response time out.", timedout);
								
								assertNotNull("No response received.", mLogResponse);
								assertFalse("Response had errors.", mLogResponse.hasErrors());
								
								GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
									@Override
									public void function() {
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
	
	@Test
	public void sendRequestThroughInstanceWithErrorHandler() throws Exception {
		mFinished = false;
		
		System.out.println("*** SendRequestThroughInstanceWithErrorHandler ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("SendRequestThroughInstanceWithErrorHandler", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
						mLogResponseEvent = new AutoResetEvent(false);
						
						request.send(new GSEventConsumer<LogEventResponse>() {
					        @Override
					        public void onEvent(LogEventResponse response) {
					        	if (response.hasErrors()) {
					        		mLogErrorResponse = response;
					        	} else {
					        		mLogResponse = response;
					        	}
								mLogResponseEvent.set();
					        }
					    });
						
						mLogResponseEvent.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
							@Override
							public void function(boolean timedout) throws Exception {
								assertFalse("Response time out.", timedout);
								
					            assertNull("Error while sending LogEvent.", mLogErrorResponse);
								assertNotNull("No response received.", mLogResponse);
								assertFalse("Response had errors.", mLogResponse.hasErrors());
								
								GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
									@Override
									public void function() {
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
	
	/*@Test
	public void sendRequestTwice() throws Exception {
		mFinished = false;
		
		System.out.println("*** SendRequestTwice ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("SendRequestTwice", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
						mLogResponseEvent = new AutoResetEvent(false);
						mLogResponseEvent1 = new AutoResetEvent(false);
						mLogResponseEvent2 = new AutoResetEvent(false);
						
						request.send(new GSEventConsumer<LogEventResponse>() {
					        @Override
					        public void onEvent(LogEventResponse response) {
					        	mLogResponse1 = response;					  
								mLogResponseEvent1.set();
								
								if (mLogResponseEvent2.isSet()) {
									mLogResponseEvent.set();
								}
					        }
					    });
						
						request.send(new GSEventConsumer<LogEventResponse>() {
					        @Override
					        public void onEvent(LogEventResponse response) {
					        	mLogResponse2 = response;					  
								mLogResponseEvent2.set();
								
								if (mLogResponseEvent1.isSet()) {
									mLogResponseEvent.set();
								}
					        }
					    });
						
						assertNull(request.getAttribute("requestId"));
						
						mLogResponseEvent.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
							@Override
							public void function(boolean timedout) throws Exception {
								GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
									@Override
									public void function() {
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
	}*/

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
