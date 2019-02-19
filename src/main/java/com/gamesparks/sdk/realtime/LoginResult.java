package com.gamesparks.sdk.realtime;

import java.util.*;

class LoginResult extends AbstractResult {
	public static ObjectPool<LoginResult>pool = new ObjectPool<LoginResult>(new ObjectPoolCreatorEvent<LoginResult>() {
		@Override
		public LoginResult onEvent() {
			return new LoginResult();
		}
	}, new ObjectPoolRefresherEvent<LoginResult>() {
		@Override
		public void onEvent(LoginResult instance) {
			instance.activePeers = new ArrayList<>();
			instance.fastPort = null;
			instance.reconnectToken = null;
			instance.peerId = null;
		}
	});

	private boolean success;
	private Integer fastPort;
	private Integer peerId;
	private String reconnectToken;
	private List<Integer> activePeers;
	
	public LoginResult() {
		super();
		
		success = false;
		reconnectToken = null;
		peerId = null;
		activePeers = new ArrayList<>();
		fastPort = null;
	}
	
	@Override
	public void execute() {
		session.connectToken = reconnectToken;
		session.peerId = peerId;

		if (packet.reliable == null || packet.reliable.booleanValue()) {
			if (fastPort != null && fastPort.intValue() != 0) {
				session.fastPort = fastPort.intValue();
			}

			session.activePeers = new ArrayList<>();
	    	session.activePeers.addAll(activePeers);
	    	session.setConnectState(GameSparksRT.ConnectState.RELIABLE_ONLY);
	    	session.connectFast();
	    	session.log("LoginResult", GameSparksRT.LogLevel.DEBUG, session.peerId + " TCP LoginResult, ActivePeers " + session.activePeers.size() + " FastPort " + session.fastPort);
		} else {
	    	session.setConnectState(GameSparksRT.ConnectState.RELIABLE_AND_FAST_SEND);
	    	session.log("LoginResult", GameSparksRT.LogLevel.DEBUG, session.peerId + " UDP LoginResult, ActivePeers " + session.activePeers.size());
		}
		
		pool.push(this);
	}
	
	public boolean executeAsync() {
		return false;
	}
	
	public static LoginResult deserialize(Stream stream, LoginResult instance) throws Exception {  
		if (instance.activePeers == null) {
		    instance.activePeers = new ArrayList<>();
		}
			  
		while (true) {
			int keyByte = stream.readByte();
			
			if (keyByte == -1) {
				break;
			}
			
			boolean _continue = false;
			
			if (keyByte == 8) {
				instance.success = ProtocolParser.readBool(stream);
			  
				_continue = true;
			} else if (keyByte == 18) {
				instance.reconnectToken = ProtocolParser.readString(stream);
			  
				_continue = true;
			} else if (keyByte == 24) {
				instance.peerId = (int)ProtocolParser.readUInt64(stream);
			  
				_continue = true;
			} else if (keyByte == 32) {
				instance.activePeers.add((int)ProtocolParser.readUInt64(stream));
			  
				_continue = true;
			} else if (keyByte == 40) {
				instance.fastPort = (int)ProtocolParser.readUInt64(stream);
			  
				_continue = true;
			}
			
			if (!_continue) {
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
		}
		
	  	return instance;
	}
}
