package com.gamesparks.sdk.realtime;

import java.util.*;

class PooledObjects {
	public static ObjectPool<List<Byte>>byteBufferPool = new ObjectPool<List<Byte>>(new ObjectPoolCreatorEvent<List<Byte>>() {
		@Override
		public List<Byte> onEvent() {
			List<Byte> buf = new ArrayList<>(256);
			
			for (int i = 0; i < 256; i ++) {
				buf.add((byte)0);
			}
			
			return buf;
		}
	});
	
	public static ObjectPool<PositionStream>positionStreamPool = new ObjectPool<PositionStream>(new ObjectPoolCreatorEvent<PositionStream>() {
		@Override
		public PositionStream onEvent() {
			return new PositionStream();
		}
	});
	
	public static ObjectPool<LimitedPositionStream>limitedPositionStreamPool = new ObjectPool<LimitedPositionStream>(new ObjectPoolCreatorEvent<LimitedPositionStream>() {
		@Override
		public LimitedPositionStream onEvent() {
			return new LimitedPositionStream();
		}
	});
	
	public static ObjectPool<Stream>memoryStreamPool = new ObjectPool<Stream>(new ObjectPoolCreatorEvent<Stream>() {
		@Override
		public Stream onEvent() {
			return new Stream();
		}
	}, new ObjectPoolRefresherEvent<Stream>() {
		@Override
		public void onEvent(Stream stream) {
			stream.setPosition(0);
		}
	});
	
	public static ObjectPool<Packet>packetPool  = new ObjectPool<Packet>(new ObjectPoolCreatorEvent<Packet>() {
		@Override
		public Packet onEvent() {
			return new Packet();
		}
	}, new ObjectPoolRefresherEvent<Packet>() {
		@Override
		public void onEvent(Packet packet) {
			packet.reset();
		}
	});
	
	public static ObjectPool<CustomRequest>customRequestPool = new ObjectPool<CustomRequest>(new ObjectPoolCreatorEvent<CustomRequest>() {
		@Override
		public CustomRequest onEvent() {
			return new CustomRequest();
		}
	}, new ObjectPoolRefresherEvent<CustomRequest>() {
		@Override
		public void onEvent(CustomRequest cr) {
			cr.reset();
		}
	});
}
