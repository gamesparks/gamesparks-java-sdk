package com.gamesparks.sdk.realtime;

class LogCommand implements IRTCommand {
	public static ObjectPool<LogCommand>pool = new ObjectPool<LogCommand>(new ObjectPoolCreatorEvent<LogCommand>() {
		@Override
		public LogCommand onEvent() {
			return new LogCommand();
		}
	});
	
	private String tag;
	private String msg;
	private GameSparksRT.LogLevel level;
	private RTSession session;
	
	public LogCommand() {
		tag = null;
		msg = null;
		level = null;
		session = null;
	}
	
	public LogCommand configure(RTSession session, String tag, GameSparksRT.LogLevel level, String msg) {
		this.tag = tag;
		this.msg = msg;
		this.level = level;
		this.session = session;

	  	return this;
	}
	
	@Override
	public void execute() {
		if (GameSparksRT.shouldLog(tag, level)) {
	    	if (session.peerId != null) {
	    		GameSparksRT.logger(session.peerId + " " + tag + ":" + msg);
	    	} else {
	    		GameSparksRT.logger(" " + tag + ":" + msg);
	    	}
		}
	  
	    LogCommand.pool.push(this);
	}
}
