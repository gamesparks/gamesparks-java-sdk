package com.gamesparks.sdk.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.junit.Test;

import com.gamesparks.sdk.Assert;
import com.gamesparks.sdk.AutoResetEvent;
import com.gamesparks.sdk.GSEventConsumer;
import com.gamesparks.sdk.GSExtended;
import com.gamesparks.sdk.GSHelperMethods;
import com.gamesparks.sdk.api.autogen.GSMessageHandler;
import com.gamesparks.sdk.api.autogen.GSMessageHandler.ScriptMessage;
import com.gamesparks.sdk.api.autogen.GSRequestBuilder.LogEventRequest;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder.LogEventResponse;

public class TestDurableRequests {
	
	private static final int AMOUNT_OF_REQUESTS = 3;
	
	private static boolean mFinished;
	
	private GSExtended mCurrInstance;
	private Object mBackendMessage = null;
	private LogEventResponse mLogResponse = null;
	private AutoResetEvent mMessageEvent;
	private AutoResetEvent mLogResponseEvent;
	private List<Object> mSentEvents = new ArrayList<>();
	
	@Test
	public void addCallbacksAfterRestart() throws Exception {
		mFinished = false;
		
		System.out.println("*** AddCallbacksAfterRestart ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("AddCallbacksAfterRestart", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				mCurrInstance.setDurableQueuePaused(true);
				
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						mCurrInstance.clearDurableQueue();
						
						LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
						mCurrInstance.setNetworkAvailable(false);
						
						request.setDurable(true);
						request.send(null);
						
						assertTrue(mCurrInstance.getSizePersistentQueue() == 1);
						
						GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
							@Override
							public void function() {
								mCurrInstance.setNetworkAvailable(true);
								
								mCurrInstance = GSHelperMethods.createInstanceAndConnect("AddCallbacksAfterRestart");
								
								mCurrInstance.setDurableQueuePaused(false);
								
								final AutoResetEvent callbackEvent = new AutoResetEvent(false);
								
								mCurrInstance.setOnAvailable(new GSEventConsumer<Boolean>() {
									@Override
									public void onEvent(Boolean available) {
										if (available) {
											mCurrInstance.setCallbackDurableRequest(0, new GSEventConsumer<LogEventResponse>() {
										        @Override
										        public void onEvent(LogEventResponse response) {
										        	callbackEvent.set();
										        }
										    });
										}
									}
								});
								
								callbackEvent.waitOne(10000, new AutoResetEvent.Callback() {	
									@Override
									public void function(boolean timedout) throws Exception {
										assertFalse("Callback not called after durable restart.", timedout);
										
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
			}
		});
		
		waitFinishedTest();
	}

	@Test
	public void checkDurableSendOrder() throws Exception {
		mFinished = false;
		
		System.out.println("*** CheckDurableSendOrder ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("CheckDurableSendOrder", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() {
						int i;
						
						mCurrInstance.setNetworkAvailable(false);
						
						for (i = 0; i < AMOUNT_OF_REQUESTS; i ++) {
							LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
							request.setDurable(true);
							request.setEventAttribute("foo", String.valueOf(i));
							request.send(null);
						}
						
						mCurrInstance.getMessageHandler().setScriptMessageListener(new GSEventConsumer<GSMessageHandler.ScriptMessage>() {
							@Override
							public void onEvent(ScriptMessage event) {
								Object value = event.getData().getAttribute("foo");
								
								if (value != null) {
									mSentEvents.add(value);
								}
							}
						});
						
						mCurrInstance.setNetworkAvailable(true);
						
						GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
							@Override
							public void function() throws Exception {
								int i;
								
								GSHelperMethods.shutDownAllInstances();
								
								assertTrue(mSentEvents.size() == AMOUNT_OF_REQUESTS);
								
								for (i = 0; i < mSentEvents.size(); i ++)
								{
									assertEquals(mSentEvents.get(i), String.valueOf(i));
								}
								
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
	public void durableNamedInstance() throws Exception {
		mFinished = false;
		
		System.out.println("*** DurableNamedInstance ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("DurableNamedInstance_1", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						mCurrInstance.clearDurableQueue();
						
						LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
						mCurrInstance.setNetworkAvailable(false);
						
						mMessageEvent = new AutoResetEvent(false);
						
						mCurrInstance.getMessageHandler().setScriptMessageListener(new GSEventConsumer<GSMessageHandler.ScriptMessage>() {
							@Override
							public void onEvent(ScriptMessage event) {
								mBackendMessage = event;
								mMessageEvent.set();	
							}
						});
					
						request.setDurable(true);
						request.send(null);
						
						assertTrue(mCurrInstance.getSizePersistentQueue() > 0);
						
						GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
							@Override
							public void function() throws Exception {
								assertNull(mBackendMessage);
								
								mCurrInstance.setNetworkAvailable(true);
								
								mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("DurableNamedInstance_1", new GSHelperMethods.Callback() {
									@Override
									public void function() {
										mMessageEvent.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
											@Override
											public void function(boolean timedout) throws Exception {
												assertFalse("Durable was never sent.", timedout);
												
												assertNotNull(mBackendMessage);
												
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
					}
				});
			}
		});
		
		waitFinishedTest();
	}
	
	@Test
	public void durableReconnect() throws Exception {
		mFinished = false;
		
		System.out.println("*** DurableReconnect ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("DurableReconnect", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() {
						mCurrInstance.clearDurableQueue();
						
						LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
						mCurrInstance.setNetworkAvailable(false);
					
						request.setDurable(true);
						request.send(null);
						
						GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
							@Override
							public void function() throws Exception {
								mCurrInstance.setNetworkAvailable(true);
								
								mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("DurableReconnect", new GSHelperMethods.Callback() {
									@Override
									public void function() {
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
								
								assertTrue(mCurrInstance.getSizePersistentQueue() == 1);
							}
						});
					}
				});
			}
		});
		
		waitFinishedTest();
	}
	
	@Test
	public void iterateDurableEntries() throws Exception {
		mFinished = false;
		
		System.out.println("*** IterateDurableEntries ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("IterateDurableEntries", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						int i;
						
						mCurrInstance.setNetworkAvailable(false);
						
						for (i = 0; i < AMOUNT_OF_REQUESTS; i ++) {
							LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
							request.setDurable(true);
							request.setEventAttribute("foo", String.valueOf(i));
							request.send(null);
						}
						
						assertTrue(mCurrInstance.getSizePersistentQueue() == AMOUNT_OF_REQUESTS);
						
						for (i = 0; i < AMOUNT_OF_REQUESTS; i ++) {
							assertTrue("error foo", mCurrInstance.compareAttributeDurableRequest(i, "foo", String.valueOf(i)));
						}
						
						mCurrInstance.setNetworkAvailable(true);
						
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
		
		waitFinishedTest();
	}
	
	@Test
	public void pauseDurableQueue() throws Exception {
		mFinished = false;
		
		System.out.println("*** PauseDurableQueue ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("PauseDurableQueue", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						mCurrInstance.clearDurableQueue();
						
						mCurrInstance.setDurableQueuePaused(true);
						
						LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
						mCurrInstance.setNetworkAvailable(false);
					
						request.setDurable(true);
						request.send(null);
						
						assertTrue(mCurrInstance.getSizePersistentQueue() == 1);
						
						GSHelperMethods.waitForShutdown(mCurrInstance, new GSHelperMethods.Callback() {
							@Override
							public void function() throws Exception {
								mCurrInstance.setNetworkAvailable(true);
								
								mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("PauseDurableQueue", new GSHelperMethods.Callback() {
									@Override
									public void function() throws Exception {
										assertTrue(mCurrInstance.getSizePersistentQueue() == 1);
										
										mCurrInstance.setDurableQueuePaused(false);
										
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
			}
		});
	
		waitFinishedTest();
	}
	
	@Test
	public void removeDurableEntry() throws Exception {
		mFinished = false;
		
		System.out.println("*** RemoveDurableEntry ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("RemoveDurableEntry", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() throws Exception {
						mCurrInstance.setNetworkAvailable(false);
						
						LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
						request.setDurable(true);
						request.send(null);
						
						assertTrue(mCurrInstance.getSizePersistentQueue() == 1);
						
						mCurrInstance.removeDurableRequest(0);
						
						assertTrue(mCurrInstance.getSizePersistentQueue() == 0);
						
						mCurrInstance.setNetworkAvailable(true);
						
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
		
		waitFinishedTest();
	}
	
	@Test
	public void restartDurableQueue() throws Exception {
		mFinished = false;
		
		System.out.println("*** RestartDurableQueue ***");
		
		mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("RestartDurableQueue", new GSHelperMethods.Callback() {
			@Override
			public void function() throws Exception {
				mCurrInstance.setDurableQueuePaused(true);
				
				GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
					@Override
					public void function() {
						mCurrInstance.clearDurableQueue();
						
						LogEventRequest request = GSHelperMethods.getLogRequest(mCurrInstance);
						
						mLogResponseEvent = new AutoResetEvent(false);
						
						request.setDurable(true);
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
								assertTrue("Durable was sent, when it should not.", timedout);
								
								assertNull(mLogResponse);
								
								assertTrue(mCurrInstance.getSizePersistentQueue() == 1);
								
								mCurrInstance.setDurableQueuePaused(false);
								
								mLogResponseEvent.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
									@Override
									public void function(boolean timedout) throws Exception {
										assertFalse("Timeout. Durable was not sent.", timedout);
										
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
