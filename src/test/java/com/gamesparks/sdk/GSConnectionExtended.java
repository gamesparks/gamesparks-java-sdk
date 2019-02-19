package com.gamesparks.sdk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;

import com.gamesparks.sdk.GS.QueuedRequest;

public class GSConnectionExtended extends GSConnection {
	
	public GSConnectionExtended(GS gs) throws Exception {
		super(gs);
	}
	
	void sendFake(QueuedRequest request) {
		pendingQueue.put(request.request.getAttribute("requestId").toString(), request);
		String json = JSONObject.toJSONString(request.request.getBaseData());
		gs.platform.logMessage(json);
	}
}
