package com.gamesparks.sdk.realtime;

import java.util.*;

public class GameSparksRT {
	public enum LogLevel {
		DEBUG(0),
		INFO(1),
		WARN(2),
		ERROR(3); 
	
		private final int value;
		
	    LogLevel(int value) { 
	    	this.value = value;
	    }
	    
	    public int getValue() { 
	    	return value;
	    }
	}
	
	public enum DeliveryIntent {
		RELIABLE(0),
		UNRELIABLE(1),
		UNRELIABLE_SEQUENCED(2);
		
		private final int value;
		
		DeliveryIntent(int value) { 
	    	this.value = value;
	    }
	    
	    public int getValue() { 
	    	return value;
	    }
	}
	
	enum ConnectState {
		DISCONNECTED(0),
		CONNECTING(1),
		RELIABLE_ONLY(2),
		RELIABLE_AND_FAST_SEND(3),
		RELIABLE_AND_FAST(4);
				
		private final int value;
		
	    ConnectState(int value) { 
	    	this.value = value;
	    }
	    
	    public int getValue() { 
	    	return value;
	    }
	}
	
	static final int MAX_RTDATA_SLOTS = 128;
	static final int MAX_UNRELIABLE_MESSAGE_SIZE_BYTES = 1400;
	
	static int retryBase = 2000;
	static int retryMax = 60000;
	static int handshakeOffset = 2000;

	private static LogLevel logLevel = LogLevel.DEBUG;
	private static Map<String, LogLevel> tagLevels = new HashMap<String, LogLevel>();
	private static Random random = new Random();
	
	public static void setRootLogLevel(LogLevel level) {
		logLevel = level;
	}

	public static void setLogLevel(String tag, LogLevel level) {
		tagLevels.put(tag, level);
	}
	
	public static RTSession getSession(String connectToken, String hostName, int tcpPort, int udpPort, IRTSessionListener listener) {
		return new RTSession(connectToken, hostName, tcpPort, udpPort, listener);
	}
	
	protected static int computeSleepPeriod(int attempt) {
		return random.nextInt(Math.min(retryMax, retryBase * (int)Math.pow(2, attempt)));
	}
	
	public static boolean shouldLog(String tag, LogLevel level)
	{
		if (tagLevels.containsKey(tag)) {
			return tagLevels.get(tag).value >= level.value;
		}
		
		return logLevel.value <= level.value;
	}
	
	public static void logger(String msg) {
		System.out.println(msg);
	}
}
