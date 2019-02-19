package com.gamesparks.sdk.realtime;

import java.util.List;

class CustomRequest extends RTRequest {
	private List<Byte> payload = null;
	
	public CustomRequest() {
		super();
	}
	
	public void configure(int opCode, GameSparksRT.DeliveryIntent intent, List<Byte>payload, RTData data, List<Integer>targetPlayers) {
		this.opCode = opCode;
		this.payload = payload;
		this.intent = intent;
		this.data = data;
		
		if (targetPlayers != null) {
			this.targetPlayers.addAll(targetPlayers);
		}
	}
	
	public void serialize(Stream stream) throws Exception {
		if (payload != null) {
			stream.writeBytes(payload, 0, payload.size());
		}
	}

	public void reset() {
		super.reset();
		
		payload = null;
	}
}
