package com.gamesparks.sdk.realtime;

class CommandFactory {
	public enum OpCodes {
		LOGIN_RESULT(-1), 
		PING_RESULT(-3), 
		UDP_CONNECT_MESSAGE(-5), 
		PLAYER_READY_MESSAGE(-7), 
		PLAYER_CONNECT_MESSAGE(-101), 
		PLAYER_DISCONNECT_MESSAGE(-103);
	
		private final int value;
		
		OpCodes(int value) { 
	    	this.value = value;
	    }
	    
	    public int getValue() { 
	    	return value;
	    }
	}
	
	public static IRTCommand getCommand(int opCode, int sender, Integer sequence, Stream stream, RTSession session, RTData data, int packetSize) throws Exception {
		IRTCommand ret = null;
		int limit = ProtocolParser.readUInt32(stream);
		LimitedPositionStream lps = PooledObjects.limitedPositionStreamPool.pop();
				  
		lps.wrap(stream, limit);
		
		if (opCode == OpCodes.LOGIN_RESULT.getValue()) {
			ret = LoginResult.deserialize(lps, LoginResult.pool.pop());
		} else if (opCode == OpCodes.PING_RESULT.getValue()) {
	    	ret = PingResult.deserialize(lps, PingResult.pool.pop());
		} else if (opCode == OpCodes.UDP_CONNECT_MESSAGE.getValue()) {
	    	ret = UDPConnectMessage.deserialize(lps, UDPConnectMessage.pool.pop());
		} else if (opCode == OpCodes.PLAYER_CONNECT_MESSAGE.getValue()) {
	    	ret = PlayerConnectMessage.deserialize(lps, PlayerConnectMessage.pool.pop());
		} else if (opCode == OpCodes.PLAYER_DISCONNECT_MESSAGE.getValue()) {
	    	ret = PlayerDisconnectMessage.deserialize(lps, PlayerDisconnectMessage.pool.pop());
		} else {
	    	if (session.shouldExecute(sender, sequence)) {
	    		ret = CustomCommand.pool.pop().configure(opCode, sender, lps, data, limit, session, packetSize);
	    	}
		}
	  
	    lps.skipToEnd();
	        
	    PooledObjects.limitedPositionStreamPool.push(lps);
	  
	    return ret;
	}
}
