package com.gamesparks.sdk.realtime;

public interface IRTSessionListener {
	public void onPlayerConnect(int peerId);

	public void onPlayerDisconnect(int peerId);

	public void onReady(boolean ready);

	public void onPacket(RTPacket packet);
}
