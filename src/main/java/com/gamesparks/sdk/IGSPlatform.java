package com.gamesparks.sdk;

import java.io.File;

import com.gamesparks.sdk.api.*;

public interface IGSPlatform {
	String getDeviceId();
	String getDeviceOS();
	String getPlatform();
	String getSDK();
	String getDeviceType();
    GSData getDeviceStats();
    
    String getPlayerId();
	String getAuthToken();
	void setPlayerId(String playerId);
	void setAuthToken(String authToken);

	File getWritableLocation();
	
	void executeOnMainThread(Runnable job);

	Object getHmac(String nonce, String secret);
	
	void logMessage(String msg);
	void logError(Throwable t);
}
