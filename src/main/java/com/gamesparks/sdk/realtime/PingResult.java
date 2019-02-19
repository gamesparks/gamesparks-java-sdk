package com.gamesparks.sdk.realtime;

class PingResult extends AbstractResult {
	public static ObjectPool<PingResult>pool = new ObjectPool<PingResult>(new ObjectPoolCreatorEvent<PingResult>() {
		@Override
		public PingResult onEvent() {
			return new PingResult();
		}
	});
	
	public PingResult() {
		super();
	}
	
	@Override
	public void execute() {
		session.log("PingResult", GameSparksRT.LogLevel.DEBUG, "");
	  
		pool.push(this);
	}
	
	public boolean executeAsync() {
		return false;
	}
	
	public static PingResult deserialize(Stream stream, PingResult instance) throws Exception {  
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
