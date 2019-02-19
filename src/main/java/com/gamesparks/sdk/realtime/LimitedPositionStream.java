package com.gamesparks.sdk.realtime;

import java.util.List;

public class LimitedPositionStream extends PositionStream {
	private int limit;
	
	public LimitedPositionStream() {
		super();
		
		limit = 0;
	}
	
	public void wrap(Stream baseStream, int limit) {
		super.wrap(baseStream);
		
		this.limit = limit;
	}
	
	public int read(List<Byte> buffer, int offset, int count) throws Exception {
		int toRead;
		
		if (count > limit - bytesRead) {
			toRead = limit - bytesRead;
		} else {
			toRead = count;
		}
		
		return super.read(buffer, offset, toRead);
	}
	
	public int readByte() throws Exception {
		if (bytesRead >= limit) {
			return -1;
		} else {
			return super.readByte();
		}
	}
	
	public void skipToEnd() throws Exception {
		if (bytesRead < limit) {
			List<Byte> discardBytes = PooledObjects.byteBufferPool.pop();
	    
			while (read(discardBytes, bytesRead, 256) == 256) {
			}
	    
			PooledObjects.byteBufferPool.push(discardBytes);
		}
	}
}
