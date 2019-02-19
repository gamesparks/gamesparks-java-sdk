package com.gamesparks.sdk.realtime;

class CustomCommand implements IRTCommand {
	public static ObjectPool<CustomCommand>pool = new ObjectPool<CustomCommand>(new ObjectPoolCreatorEvent<CustomCommand>() {
		@Override
		public CustomCommand onEvent() {
			return new CustomCommand();
		}
	});
	
	private int opCode;
	private int sender;
	private int limit;
	private int packetSize;
	private RTSession session;
	private Stream ms;
	private LimitedPositionStream limitedStream;
	private RTData data;
	
	public CustomCommand() {
		opCode = 0;
		sender = 0;
		limit = 0;
		packetSize = 0;
		session = null;
		ms = null;
		limitedStream = null;
		data = null;
	}
	
	public CustomCommand configure(int opCode, int sender, PositionStream lps, RTData data, int limit, RTSession session, int packetSize) throws Exception {
		ms = PooledObjects.memoryStreamPool.pop();
	  	this.packetSize = packetSize;
	  	this.opCode = opCode;
	  	this.sender = sender;
	  	this.data = data;
	  	this.session = session;
	  	this.limit = limit;
	  	limitedStream = null;

	  	if (lps != null) {
		    limitedStream = PooledObjects.limitedPositionStreamPool.pop();
	
		    for (int i = 0; i < limit; i ++) {
		    	int read = lps.readByte();
		      
		    	ms.writeByte(read);
		    }
		    ms.setPosition(0);
		    limitedStream.wrap(ms, limit);
	  	}

	  	return this;
	}
	
	@Override
	public void execute() {
		session.listener.onPacket(new RTPacket(opCode, sender, limitedStream, limit, data, packetSize));
		  
		PooledObjects.memoryStreamPool.push(ms);
		PooledObjects.limitedPositionStreamPool.push(limitedStream);
		CustomCommand.pool.push(this);
	}
}
