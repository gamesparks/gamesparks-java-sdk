package com.gamesparks.sdk.realtime;

import java.util.List;

import com.gamesparks.sdk.realtime.Key.Wire;

class RTDataSerializer {
	public static ObjectPool<RTData>cache = new ObjectPool<RTData>(new ObjectPoolCreatorEvent<RTData>() {
		@Override
		public RTData onEvent() {
			return new RTData();
		}
	}, new ObjectPoolRefresherEvent<RTData>() {
		@Override
		public void onEvent(RTData rtData) {
			for (int i= 0; i < rtData.data.length; i ++) {
				if (rtData.data[i].dataVal != null) {
					rtData.data[i].dataVal.dispose();
				}
				rtData.data[i].reset();
			}
		}
	});
	
	public static RTData get() {
	  return cache.pop();
	}
	
	public static RTData readRTData(Stream stream, RTData instance) throws Exception {
		if (instance == null) {
			instance = cache.pop();
		}
	  
		int limit = ProtocolParser.readUInt32(stream);
	 
		limit += stream.getPosition();
	  
		while (true) {
			if (stream.getPosition() >= limit) {
				if (stream.getPosition() == limit) {
					break;
				} else {
					System.err.println("WARNING: Read past max limit");
	        
					break;
				}
			}
	    
			int keyByte = stream.readByte();
	    
			if (keyByte == -1) {
				break;
			}
	    
			Key key = ProtocolParser.readKey(keyByte, stream);
	    
		    if (key.wireType == Wire.VARINT.getValue()) {
		      instance.data[key.field] = new RTVal(ProtocolParser.readZInt64(stream));
		    } else if (key.wireType == Wire.FIXED32.getValue()) {
		      instance.data[key.field] = new RTVal(ProtocolParser.readSingle(stream));
			} else if (key.wireType == Wire.FIXED64.getValue()) {
		      instance.data[key.field] = new RTVal(ProtocolParser.readDouble(stream));
			} else if (key.wireType == Wire.LENGTH_DELIMITED.getValue()) {
		      instance.data[key.field] = RTVal.deserializeLengthDelimited(stream);
			}
		        
		    if (key.field == 0) {
		    	System.err.println("WARNING: Invalid field id: 0, something went wrong in the stream");
		      
		      	break;
		    }
		}
	  
	  	return instance;
	}
	
	public static void writeRTData(Stream stream, RTData instance) throws Exception {
		Stream ms = PooledObjects.memoryStreamPool.pop();
	  
		for (int index = 0; index < instance.data.length; index ++) {
			RTVal entry = instance.data[index];
	    
			if (entry.longVal != null) {
				ProtocolParser.writeUInt32(ms, index << 3);
	      
				ProtocolParser.writeZInt64(ms, entry.longVal);
			} else if (entry.floatVal != null) {
				ProtocolParser.writeUInt32(ms, (index << 3) | 5);
	        
				ProtocolParser.writeSingle(ms, entry.floatVal);
			} else if (entry.doubleVal != null) {
				ProtocolParser.writeUInt32(ms, (index << 3) | 1);
	        
				ProtocolParser.writeDouble(ms, entry.doubleVal);
			} else if (entry.dataVal != null || entry.stringVal != null || entry.vecVal != null) {
				ProtocolParser.writeUInt32(ms, (index << 3) | 2);
	    
				entry.serializeLengthDelimited(ms);
			}
		}
	  
		List<Byte> buffer = ms.getBuffer();
	  
		ProtocolParser.writeBytes(stream, buffer, ms.getPosition());

		PooledObjects.memoryStreamPool.push(ms); 
	}
}
