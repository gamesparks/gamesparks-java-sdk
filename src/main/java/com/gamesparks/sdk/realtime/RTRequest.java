package com.gamesparks.sdk.realtime;

import java.util.*;

class RTRequest {
	protected int opCode;
	protected RTData data;
	protected List<Integer> targetPlayers;
	protected GameSparksRT.DeliveryIntent intent;
	
	public RTRequest() {
		data = null;
		opCode = 0;
		targetPlayers = new ArrayList<>();
		intent = GameSparksRT.DeliveryIntent.RELIABLE;
	}
	
	public Packet toPacket(RTSession session, boolean fast) {
		Packet p = PooledObjects.packetPool.pop();
				  
		p.opCode = opCode;
		p.data = data;
		p.session = session;

		if (!fast && intent != GameSparksRT.DeliveryIntent.RELIABLE) {
			p.reliable = false;
		}

		if (intent == GameSparksRT.DeliveryIntent.UNRELIABLE_SEQUENCED) {
			p.sequenceNumber = session.nextSequenceNumber();
		}

		if (targetPlayers.size() > 0) {
			p.targetPlayers = targetPlayers;
		}

		p.request = this;
				  
		return p;
	}

	public void reset() {
		targetPlayers.clear();
	}
	
	public void serialize(Stream stream) throws Exception {
	}
}
