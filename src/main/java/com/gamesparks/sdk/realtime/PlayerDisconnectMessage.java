package com.gamesparks.sdk.realtime;

import java.util.*;

class PlayerDisconnectMessage extends AbstractResult {
	public static ObjectPool<PlayerDisconnectMessage>pool = new ObjectPool<PlayerDisconnectMessage>(new ObjectPoolCreatorEvent<PlayerDisconnectMessage>() {
		@Override
		public PlayerDisconnectMessage onEvent() {
			return new PlayerDisconnectMessage();
		}
	}, new ObjectPoolRefresherEvent<PlayerDisconnectMessage>() {
		@Override
		public void onEvent(PlayerDisconnectMessage instance) {
			instance.activePeers = new ArrayList<>();
		}
	});
	
	private int peerId;
	private List<Integer> activePeers;
	
	public PlayerDisconnectMessage() {
		super();
		
		peerId = 0;
		activePeers = new ArrayList<>();
	}
	
	@Override
	public void execute() {
		session.activePeers = new ArrayList<>();
		session.activePeers.addAll(activePeers);
		session.log("PlayerDisconnectMessage", GameSparksRT.LogLevel.DEBUG, "PeerId=" + peerId + ", ActivePeers " + session.activePeers.size());
		session.onPlayerDisconnect(peerId);
		  
		pool.push(this);
	}
	
	public static PlayerDisconnectMessage deserialize(Stream stream, PlayerDisconnectMessage instance) throws Exception {  
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
