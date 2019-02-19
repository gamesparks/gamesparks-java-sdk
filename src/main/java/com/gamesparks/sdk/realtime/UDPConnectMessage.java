package com.gamesparks.sdk.realtime;

import com.gamesparks.sdk.realtime.CommandFactory.OpCodes;

class UDPConnectMessage extends AbstractResult {
	public static ObjectPool<UDPConnectMessage>pool = new ObjectPool<UDPConnectMessage>(new ObjectPoolCreatorEvent<UDPConnectMessage>() {
		@Override
		public UDPConnectMessage onEvent() {
			return new UDPConnectMessage();
		}
	});
	
	public UDPConnectMessage() {
		super();
	}
	
	@Override
	public void execute() {
		boolean reliable;
		  
		if (packet.reliable != null) {
			reliable = packet.reliable.booleanValue();
		} else {
		    reliable = false;
		}
		  
		session.log("UDPConnectMessage", GameSparksRT.LogLevel.DEBUG, "(UDP) reliable=" + reliable + ", ActivePeers " + session.activePeers.size());
		  
		if (!reliable) { 
			session.setConnectState(GameSparksRT.ConnectState.RELIABLE_AND_FAST);
			try {
				session.sendData(OpCodes.UDP_CONNECT_MESSAGE.getValue(), GameSparksRT.DeliveryIntent.RELIABLE, null, null);
			} catch (Exception e) {
				System.err.println(e);
			}
		} else {
		    session.log("UDPConnectMessage", GameSparksRT.LogLevel.DEBUG, "TCP (Unexpected) UDPConnectMessage");
		}
	  
		pool.push(this);
	}
	
	public boolean executeAsync() {
		return false;
	}
	
	public static UDPConnectMessage deserialize(Stream stream, UDPConnectMessage instance) throws Exception {  
		while (true) {
			int keyByte = stream.readByte();
	    
			if (keyByte == -1) {
				break;
			}
	    
			Key key = ProtocolParser.readKey(keyByte, stream);

			if (key.field == 0) {
				System.err.println("WARNING: Invalid field id: 0, something went wrong in the stream");
	      
				break;
			} else {
				if (!ProtocolParser.skipKey(stream, key)) {
					break;
				}
			}
		}

	  	return instance;
	}
}
