package com.gamesparks.sdk.realtime;

import java.util.*;

public class PositionStream extends Stream {
	protected int bytesRead;
	
	private Stream stream;
	private List<Byte> tempBuffer;
	
	public PositionStream() {
		bytesRead = 0;
		stream = null;
	}
	
	public void wrap(Stream baseStream) {
		bytesRead = 0;
		stream = baseStream;
	}
	
	public int read(List<Byte> buffer, int offset, int count) throws Exception {
		int read = stream.read(buffer, offset, count);
	  
		bytesRead += read;
	  
		return read;
	}
	
	public int readByte() throws Exception {
		tempBuffer = new ArrayList<>();
		
		if (read(tempBuffer, 0, 1) == 1) {
			return (int)tempBuffer.get(0) & 0xff;
		}
			  
		return -1;
	}
	
	public int seek(int offset) throws Exception {
		for (int i = 0; i < offset; i ++) {
			readByte();
		}
		
		return bytesRead;
	}
	
	public int getLength() {
		return stream.getLength();
	}

	public int getPosition() {
		return bytesRead;
	}
}
