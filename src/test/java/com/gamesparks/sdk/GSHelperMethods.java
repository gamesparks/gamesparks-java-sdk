package com.gamesparks.sdk;

import java.util.*;

import com.gamesparks.sdk.api.autogen.*;
import com.gamesparks.sdk.api.autogen.GSRequestBuilder.*;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder.*;
import com.gamesparks.sdk.GSHelperMethods.Callback;

public class GSHelperMethods {
	
	public interface Callback {
        void function() throws Exception;
    }
	
	public static final int REQUEST_TIMEOUT = 60;
	
	public static String mUserId;
	
	private static HashMap<String, GSExtended> mGSInstances = new HashMap<String, GSExtended>();
	private static Callback mCallback;
	
	public static GSExtended createInstance(String name) {
		GSExtended instance = getInstance(name);
		
		if (instance != null) {
			instance.disconnect();
		} else {
			instance = new GSExtended();
			
			mGSInstances.put(name, instance);
		}
		
		return instance;
	}
	
	public static GSExtended createInstanceAndConnect(String name) {
		GSExtended instance = createInstance(name);
		
		if (instance != null)
		{
			connectInstance(instance);
		}
		
		return instance;
	}
	
	public static GSExtended createDefaultInstance() {
		return createInstance("default");
	}
	
	public static void connectDefaultInstance() {
		connectInstance(getInstance("default"));
	}
	
	public static GSExtended getInstance(String name) {
		return mGSInstances.get(name);
	}
	
	public static GSExtended getDefaultInstance() {
		return getInstance("default");
	}
	
	public static GSExtended createInstanceWaitForAvailable(String name, Callback callback) throws Exception {
		GSExtended gs = createInstance(name);
		final AutoResetEvent ev = new AutoResetEvent(false);
		
		Assert.assertNotNull("GameSparks instance null.", gs);
		
		Assert.assertFalse("GameSparks was available from the start.", gs.isAvailable());
		
		gs.setOnAvailable(new GSEventConsumer<Boolean>() {
			@Override
			public void onEvent(Boolean available) {
				if (available) {
					ev.set();
				}
			}
		});
		
		connectInstance(gs);
		
		mCallback = callback;
		
		ev.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
			@Override
			public void function(boolean timedout) throws Exception {
				createInstanceWaitForAvailablePart2(timedout);
			}
		});
		
		return gs;
	}
	
	public static void waitForShutdown(GSExtended gs, Callback callback) {
		final AutoResetEvent ev = new AutoResetEvent(false);
		
		mCallback = callback;
		
		shutDown(gs, new Callback() {
			@Override
			public void function() {
				ev.set();
			}
		});
				
		ev.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
			@Override
			public void function(boolean timedout) throws Exception {
				waitForShutdownPart2(timedout);
			}
		});
	}
	
	public static void shutDown(final GSExtended gs) {
		gs.getRequestBuilder().createEndSessionRequest()
			.send(new GSEventConsumer<EndSessionResponse>() {
				@Override
				public void onEvent(EndSessionResponse event) {
					disconnectInstance(gs);	
				}
			});
	}
	
	public static void shutDown(final GSExtended gs, final Callback callback) {
		gs.getRequestBuilder().createEndSessionRequest()
			.send(new GSEventConsumer<EndSessionResponse>() {
				@Override
				public void onEvent(EndSessionResponse event) {
					disconnectInstance(gs);	
					
					if (callback != null)
					{
						try {
							callback.function();
						} catch (Exception e) {
						}
					}
				}
			});
	}
	
	public static void shutDownAllInstances() {
		Iterator it = mGSInstances.entrySet().iterator();
		
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        GSExtended gs = (GSExtended)pair.getValue();
	        
	        shutDown(gs);
	    }
	    
	    mGSInstances.clear();
	}
	
	public static void waitForDeviceAuthentication(final GSExtended gs, Callback callback) throws Exception {
		final AutoResetEvent ev = new AutoResetEvent(false);
		
		mCallback = callback;
		
		gs.getRequestBuilder().createDeviceAuthenticationRequest()
			.setDeviceId("12345")
			.setDeviceOS("MacOS")
			.send(new GSEventConsumer<AuthenticationResponse>() {
				@Override
				public void onEvent(AuthenticationResponse event) {
					mUserId = event.getUserId();
					
					ev.set();
					
					try {
						Assert.assertTrue(gs.isAuthenticated());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}		
		});
		
		ev.waitOne(GSHelperMethods.REQUEST_TIMEOUT, new AutoResetEvent.Callback() {	
			@Override
			public void function(boolean timedout) throws Exception {
				waitForDeviceAuthenticationPart2(timedout);
			}
		});
	}
	
	public static LogEventRequest getLogRequest(GSExtended gs) {
		return gs.getRequestBuilder().createLogEventRequest()
			.setEventKey("testMessage")
			.setEventAttribute("foo", "bar");
	}
	
	// -------------------------------------------------------------------------------------------------
	
	private static void disconnectInstance(GSExtended gs) {
		gs.disconnect();
	}
	
	private static void disconnectDefaultInstance() {
		disconnectInstance(getInstance("default"));
	}
	
	private static void connectInstance(GSExtended instance) {
		instance.connect();
	}
	
	private static void createInstanceWaitForAvailablePart2(boolean timedout) throws Exception {
		Assert.assertFalse("Timeout. GameSparks not available.", timedout);
		
		mCallback.function();
	}
	
	private static void waitForShutdownPart2(boolean timedout) throws Exception {
		Assert.assertFalse("Timeout. Shutdown never completed.", timedout);
		
		mCallback.function();
	}
	
	private static void waitForDeviceAuthenticationPart2(boolean timedout) throws Exception {
		Assert.assertFalse("Did not authenticate.", timedout);
		
		mCallback.function();
	}
}
