package com.gamesparks.sdk.realtime;

public class RTPacket {
	public int opCode;
	public int sender;
	public PositionStream stream;
	public int streamLength;
	public RTData data;
	public int packetSize;
	
	public RTPacket(int opCode, int sender, PositionStream limitedStream, int limit, RTData data, int packetSize) {
		this.opCode = opCode;
		this.sender = sender;
		this.stream = limitedStream;
		this.streamLength = limit;
		this.data = data;
		this.packetSize = packetSize;
	}
	
	public String toString() {
		return "OpCode=" + opCode + ",Sender=" + sender + ",streamExists=" + (stream != null) + (stream == null ? "" : ",StreamLength=" + streamLength) + ",Data=" + (data != null ? data.toString() : ".PacketSize=" + packetSize);
	}
}
