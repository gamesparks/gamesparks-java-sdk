package com.gamesparks.sdk.realtime;

import java.util.*;

class Packet {
	public int opCode = 0;
	public Integer sequenceNumber = null;
	public Integer requestId = null;
	public List<Integer> targetPlayers = null;
	public Integer sender = null;
	public Boolean reliable = null;
	public RTData data = null;
	public List<Byte> payload = null;
	public RTRequest request = null;
	public boolean hasPayload = false;
	public IRTCommand command = null;
	public RTSession session = null;
	
	public Packet() {
	}
	
	public void reset() {
		opCode = 0;
		sequenceNumber = null;
		requestId = null;
		targetPlayers = null;
		sender = null;
		reliable = null;
		payload = null;
		command = null;
		request = null;
		hasPayload = false;
		data = null;
	}
	
	public String toString() {
		return "{OpCode:" + opCode + ",TargetPlayers:" + targetToString() + "}";
	}

	public String targetToString() {
		StringBuilder s = new StringBuilder("[");

		if (targetPlayers != null) {
	    	for (int i = 0; i < targetPlayers.size(); i ++) { 
	    		s.append(targetPlayers.get(i) + " ");
	    	}
		}

	    return s.append("]").toString();
	}
	
	public List<Byte> readPayload(Stream stream, int packetSize) throws Exception {
		hasPayload = true;
		if (sender != null) {
			command = CommandFactory.getCommand(opCode, sender, sequenceNumber, stream, session, data, packetSize);
		} else {
			command = CommandFactory.getCommand(opCode, 0, sequenceNumber, stream, session, data, packetSize);
		}
		
		return null;
	}
	
	public void writePayload(Stream stream) throws Exception {
		if (request != null) {
			Stream ms = PooledObjects.memoryStreamPool.pop();
	   
			request.serialize(ms);
	    
			List<Byte> written = ms.getBuffer();

			if (ms.getPosition() > 0) {
				stream.writeByte(122);
				ProtocolParser.writeBytes(stream, written, ms.getPosition());
			}
	    
			PooledObjects.memoryStreamPool.push(ms);
		} else {
			if (payload != null) {
				stream.writeByte(122);
				ProtocolParser.writeBytes(stream, payload, payload.size());
			}
		}
	}
	
	public static Stream serialize(Stream stream, Packet instance) throws Exception {
		stream.writeByte(8);
		ProtocolParser.writeZInt32(stream, instance.opCode);
	  
		if (instance.sequenceNumber != null) {
			stream.writeByte(16);
	    	ProtocolParser.writeUInt64(stream, instance.sequenceNumber);
		}
	  
		if (instance.requestId != null) {
			stream.writeByte(24);
	    	ProtocolParser.writeUInt64(stream, instance.requestId);
		}
	  
		if (instance.targetPlayers != null) {
			for (int i = 0; i < instance.targetPlayers.size(); i ++) {
				stream.writeByte(32);
				ProtocolParser.writeUInt64(stream, instance.targetPlayers.get(i));
			}
		}
	  
		if (instance.sender != null) {
			stream.writeByte(40);
	    	ProtocolParser.writeUInt64(stream, instance.sender);
		}
	  
		if (instance.reliable != null) {
			stream.writeByte(48);
			ProtocolParser.writeBool(stream, instance.reliable);
		}
	  
		if (instance.data != null) {
			stream.writeByte(114);
	    	RTData.writeRTData(stream, instance.data);
		}
	           
	  	instance.writePayload(stream);
	  
	  	return stream;
	}
	
	public static int serializeLengthDelimited(Stream stream, Packet instance) throws Exception
	{
		int ret;
		Stream ms = PooledObjects.memoryStreamPool.pop();
	  
		Packet.serialize(ms, instance);
	  
		List<Byte> data = ms.getBuffer();
	  
		ProtocolParser.writeBytes(stream, data, ms.getPosition());
	  
		ret = ms.getPosition();
	  
		PooledObjects.memoryStreamPool.push(ms);
	  
	  	return ret;
	}
	
	public static int deserializeLengthDelimited(Stream stream, Packet instance) throws Exception
	{
		int limit = ProtocolParser.readUInt32(stream);
		int origLimit = limit;
	  
		limit += stream.getPosition();
	  
		while (true) {
			//System.out.println("pos " + stream.getPosition()+" "+limit);
			if (stream.getPosition() >= limit) {
				if (stream.getPosition() == limit) {
					break;
				} else {
					System.err.println("WARNING: Read past max limit");
	        
					break;
				}
			}
	    
			int keyByte = stream.readByte();
			//System.out.println("key " + keyByte + " "+stream.getPosition());
	  
			if (keyByte == -1) {
				System.err.println("WARNING: End of stream");
	        
				break;
			}
	    
			boolean _continue = false;
	    
			if (keyByte == 8) {
				instance.opCode = ProtocolParser.readZInt32(stream);
	     
				_continue = true;
			} else if (keyByte == 16) {
				instance.sequenceNumber = (int)ProtocolParser.readUInt64(stream);
	      
				_continue = true;
			} else if (keyByte == 24) {
				instance.requestId = (int)ProtocolParser.readUInt64(stream);
	      
				_continue = true;
			} else if (keyByte == 40) {
				instance.sender = (int)ProtocolParser.readUInt64(stream);
	      
				_continue = true;
			} else if (keyByte == 48) {
				instance.reliable = ProtocolParser.readBool(stream);
	      
				_continue = true;
			} else if (keyByte == 114) {
				if (instance.data == null) {
					instance.data = RTData.readRTData(stream, instance.data);
				} else {
					RTData.readRTData(stream, instance.data);
				}
	      
				_continue = true;
			} else if (keyByte == 122) {
				instance.payload = instance.readPayload(stream, origLimit);
	      
				_continue = true;
			}
	    
			if (!_continue) {
				Key key = ProtocolParser.readKey(keyByte, stream);
				if (key.field == 0) {
					System.err.println("WARNING: Invalid field id: 0, something went wrong in the stream");
	        
					break;
				} else {
					if (!ProtocolParser.skipKey(stream, key)) {
						break;
					}
				}
			}
		}
	  
	  	return origLimit;
	}
}
