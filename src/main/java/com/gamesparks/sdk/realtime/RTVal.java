package com.gamesparks.sdk.realtime;

import java.util.List;

class RTVal {
	public Long longVal = null;
	public Float floatVal = null;
	public Double doubleVal = null;
	public RTData dataVal = null;
	public String stringVal = null;
	public RTVector vecVal = null;
	
	public RTVal() {
	}
	
	public RTVal(long value) {
		longVal = value;
	}

	public RTVal(float value) {
		floatVal = value;
	}

	public RTVal(double value) {
		doubleVal = value;
	}

	public RTVal(String value) {
		stringVal = value;
	}
	
	public RTVal(RTData value) {
		dataVal = value;
	}

	public RTVal(RTVector value) {
		vecVal = value;
	}
	
	public static RTVal deserializeLengthDelimited(Stream stream) throws Exception {
		RTVal instance = new RTVal();
	  
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
				System.err.println("WARNING: End of stream");
	        
				break;
			}
	    
	    	boolean _continue = false;
	    
	    	if (keyByte == 10) {
	    		instance.stringVal = ProtocolParser.readString(stream);
	      
	    		_continue = true;
	    	} else if (keyByte == 18) {
	    		int end2 = ProtocolParser.readUInt32(stream);
	      
	    		end2 += stream.getPosition();
	      
	    		RTVector v = new RTVector();
	      
	    		int i = 0;
	    		
	    		while (stream.getPosition() < end2) {
	    			float read = ProtocolParser.readSingle(stream);
	        
	    			if (i == 0) {
	    				v.x = read;
	    			} else if (i == 1) {
	    				v.y = read;
	    			} else if (i == 2) {
	    				v.z = read;
	    			} else if (i == 3) {
	    				v.w = read;
	    			}
	        
	    			i ++;
	    		}
	    		instance.vecVal = v;
	     
	    		if (stream.getPosition() != end2) {
	    			System.err.println("WARNING: Read too many bytes in packed data");
	        
	    			break;
	    		}
	      
	    		_continue = true;
	    	} else if (keyByte == 114) {
	    		if (instance.dataVal == null) {
	    			instance.dataVal = RTDataSerializer.cache.pop();
	    		}
	    		RTData.readRTData(stream, instance.dataVal);
	      
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
	    
	  	return instance;
	}
	
	public void serializeLengthDelimited(Stream stream) throws Exception {
		Stream ms = PooledObjects.memoryStreamPool.pop();
	   
		if (stringVal != null) {
	    	ms.writeByte(10);
	    	ProtocolParser.writeString(ms, stringVal);
		} else if (dataVal != null) {
	    	ms.writeByte(114);
	    	RTData.writeRTData(ms, dataVal); 
		} else if (vecVal != null) {
			int numberOfFloatsSet = 0;
	    	RTVector vecValue = vecVal;
	    
	    	ms.writeByte(18);
	    
	    	if (vecValue.x != null) {
	    		numberOfFloatsSet ++;
	    	}
	    	if (vecValue.y != null) {
	    		numberOfFloatsSet ++;
	    	}
	    	if (vecValue.z != null) {
	    		numberOfFloatsSet ++;
	    	}
	    	if (vecValue.w != null) {
	    		numberOfFloatsSet ++;
	    	}
	    
	    	ProtocolParser.writeUInt32(ms, 4 * numberOfFloatsSet);

	    	for (int i = 0; i < numberOfFloatsSet; i ++) {
	    		if (i == 0) {
	    			ProtocolParser.writeSingle(ms, vecValue.x);
	    		} else if (i == 1) {
	    			ProtocolParser.writeSingle(ms, vecValue.y);
	    		} else if (i == 2) {
	    			ProtocolParser.writeSingle(ms, vecValue.z);
	    		} else if (i == 3) {
	    			ProtocolParser.writeSingle(ms, vecValue.w);
	    		}
	    	}
		}
	  
	  	List<Byte> data = ms.getBuffer();
	  
	  	ProtocolParser.writeBytes(stream, data, ms.getPosition());
	  
	  	PooledObjects.memoryStreamPool.push(ms);
	}
	
	public void reset() {
		if (dataVal != null) {
			dataVal.dispose();
		}
		longVal = null;
		floatVal = null;
		doubleVal = null;
		dataVal = null;
		stringVal = null;
		vecVal = null;
	}
	
	public boolean dirty() {
		if (floatVal != null) {
			return true;
		} else if (longVal != null) {
			return true;
		} else if (doubleVal != null) {
			return true;
		} else if (dataVal != null) {
			return true;
		} else if (stringVal != null) {
			return true;
		} else if (vecVal != null) {
			return true;
		}
		
		return false;
	}

	public String asString() {
		if (floatVal != null) {
			return floatVal.toString();
		} else if (longVal != null) {
			return longVal.toString();
		} else if (doubleVal != null) {
			return doubleVal.toString();
		} else if (dataVal != null) {
			return dataVal.toString();
		} else if (stringVal != null) {
			return "\"" + stringVal + "\"";
		} else if (vecVal != null) {
			return "|" + vecVal.x + "|" + vecVal.y + "|" + vecVal.z + "|" + vecVal.w + "|";
		}
		
		return null;
	}
}
