package com.gamesparks.sdk.realtime;

abstract class Connection {
	protected boolean stopped;
	protected RTSession session;
	
	public Connection(RTSession session) {
		this.session = session;
		stopped = false;
	}
	
	public void shutdown() {
	  stopped = true;
	  stopInternal();
	}
	
	public void onPacketReceived(Packet p, int packetSize) throws Exception {
		if (p.command != null) {
			if (p.command instanceof AbstractResult) {
				AbstractResult result = (AbstractResult)p.command;
	      
				result.configure(p, session);
				if (result.executeAsync()) {
					session.submitAction(p.command);
				} else {
					p.command.execute();
				}
			} else {
				session.submitAction(p.command);
			}
		} else {
			if (!p.hasPayload) {
				CustomCommand cmd;
	      
				if (p.sender != null) {
					cmd = CustomCommand.pool.pop().configure(p.opCode, p.sender, null, p.data, 0, session, packetSize);
				} else {
					cmd = CustomCommand.pool.pop().configure(p.opCode, 0, null, p.data, 0, session, packetSize);
				}
	      
				if (cmd != null) {
					session.submitAction(cmd);
				}
			}
		}
	}
	
	abstract protected void stopInternal();
}
