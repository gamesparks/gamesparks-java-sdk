package com.gamesparks.sdk.realtime;

import java.util.*;

class PlayerConnectMessage extends AbstractResult {
	public static ObjectPool<PlayerConnectMessage>pool = new ObjectPool<PlayerConnectMessage>(new ObjectPoolCreatorEvent<PlayerConnectMessage>() {
		@Override
		public PlayerConnectMessage onEvent() {
			return new PlayerConnectMessage();
		}
	}, new ObjectPoolRefresherEvent<PlayerConnectMessage>() {
		@Override
		public void onEvent(PlayerConnectMessage instance) {
			instance.activePeers = new ArrayList<>();
		}
	});
	
	private int peerId;
	private List<Integer> activePeers;
	
	public PlayerConnectMessage() {
		super();
		
		peerId = 0;
		activePeers = new ArrayList<>();
	}
	
	@Override
	public void execute() {
		session.activePeers = new ArrayList<>();
		session.activePeers.addAll(activePeers);
		session.log("PlayerConnectMessage", GameSparksRT.LogLevel.DEBUG, "PeerId=" + peerId + ", ActivePeers " + session.activePeers.size());
		session.onPlayerConnect(peerId);
		  
		pool.push(this);
	}
	
	public static PlayerConnectMessage deserialize(Stream stream, PlayerConnectMessage instance) throws Exception {  
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
				instance.peerId = (int)ProtocolParser.readUInt64(stream);
			  
				_continue = true;
			} else if (keyByte == 32) {
				instance.activePeers.add((int)ProtocolParser.readUInt64(stream));
			  
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
