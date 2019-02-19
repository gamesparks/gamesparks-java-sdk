package com.gamesparks.sdk.realtime;

class AbstractResult implements IRTCommand {
	protected Packet packet;
	protected RTSession session;
	
	public AbstractResult() {
		packet = null;
		session = null;
	}
	
	public void configure(Packet packet, RTSession session) {
		this.packet = packet;
		this.session = session;
	}
	
	@Override
	public void execute() {
	}

	public boolean executeAsync() {
		return true;
	}
}
