package com.gamesparks.sdk.realtime;

import java.nio.charset.StandardCharsets;
import java.util.*;

import com.gamesparks.sdk.realtime.Key.Wire;

class ProtocolParser {
	public static boolean readBool(Stream stream) throws Exception {
		int b = stream.readByte();
		
		if (b < 0) {
			System.err.println("WARNING: Stream ended too early");
		} else if (b == 1) {
			return true;
		} else if (b == 0) {
			return false;
		}
		
		System.err.println("WARNING: Invalid boolean value");
		
		return false;
	}
	
	public static int readUInt32(Stream stream) throws Exception {
		int b;
		int val = 0;
		
		for (int n = 0; n <= 4; n ++) {
			b = stream.readByte();
			if (b < 0) {
				System.err.println("WARNING: Stream ended too early");
		      
				return 0;
			}
			
			if (n == 4 && (b & 0xF0) != 0) {
				System.err.println("WARNING: Got larger VarInt than 32bit unsigned");
				
				return 0;
			}
			
			if ((b & 0x80) == 0) {
				return val | (b << (7 * n));
			}
			
			val |= (b & 0x7F) << (7 * n);
		}
		
		System.err.println("WARNING: Got larger VarInt than 32bit unsigned");
		
		return 0;
	}
	
	public static int readZInt32(Stream stream) throws Exception {
		int val = readUInt32(stream);
		
		return (val >> 1) ^ ((val << 31) >> 31);
	}
	
	public static float readSingle(Stream stream) throws Exception {
		int b[] = new int[4];
		int val;
		
		for (int n = 3; n >= 0; n --) {
			b[n] = stream.readByte(); 
		}
		
		val = (b[0] << 24) | (b[1] << 16) | (b[2] << 8) | b[3];
		
		return Float.intBitsToFloat(val);
	}
	
	public static long readUInt64(Stream stream) throws Exception {
		int b;
		long val = 0;
		
		for (int n = 0; n <= 9; n ++) {
			b = stream.readByte();
			if (b < 0) {
				System.err.println("WARNING: Stream ended too early");
		      
				return 0;
			}
			
			if (n == 9 && (b & 0xFE) != 0) {
				System.err.println("WARNING: Got larger VarInt than 64 bit unsigned");
				
				return 0;
			}
			
			if ((b & 0x80) == 0) {
				return val + b * (long)Math.pow(128, n);
			}
			
			val += (b & 0x7F) * (long)Math.pow(128, n);
		}
		
		System.err.println("WARNING: Got larger VarInt than 64 bit unsigned");
		
		return 0;
	}
	
	public static long readZInt64(Stream stream) throws Exception {
		boolean sign = false;
		long val = readUInt64(stream);
		
		if (val % 2 == 1) {
			sign = true;
		}
			  
		val >>= 1;
			  
		if (sign == true) {
		    return -val;
		} else {
			return val;
		}
	}
	
	public static double readDouble(Stream stream) throws Exception {
		int b[] = new int[8];
		long val;
		
		for (int n = 7; n >= 0; n --) {
			b[n] = stream.readByte(); 
		}
		
		val = ((long)b[0] << 56) | ((long)b[1] << 48) | ((long)b[2] << 40) | ((long)b[3] << 32) |
				((long)b[4] << 24) | (long)(b[5] << 16) | ((long)b[6] << 8) | (long)b[7];
		
		return Double.longBitsToDouble(val);
	}
	
	public static void readSkipVarInt(Stream stream) throws Exception {
		while (true) {
			int b = stream.readByte();
	    
			if (b < 0) {
		        System.err.println("WARNING: Stream ended too early");
		        
		        return;
			}
	
			if ((b & 0x80) == 0) {
				return;
			}
		}
	}
	
	public static String readString(Stream stream) throws Exception {
		int r;
		int length = readUInt32(stream);
		int read = 0;
		String ret;
		Stream ms = PooledObjects.memoryStreamPool.pop();
		List<Byte> buffer = PooledObjects.byteBufferPool.pop();
		
		while (read < length) {
			r = stream.read(buffer, 0, Math.min(length - read, buffer.size()));
			
			if (r == 0) {
				System.err.println("WARNING: Expected " + (length - read) + " got " + read);
				
				return "";
			}
			
			ms.writeBytes(buffer, 0, r);
			
			read += r;
		}
		
		ret = ms.toString(0, ms.getPosition());
		
		PooledObjects.byteBufferPool.push(buffer);
		PooledObjects.memoryStreamPool.push(ms);
		
		return ret;
	}
	
	public static Key readKey(int firstByte, Stream stream) throws Exception {
		if (firstByte < 128) {
		    return new Key(firstByte >>> 3, firstByte & 0x07);
		}
		  
		int fieldID = (readUInt32(stream) << 4) | ((firstByte >>> 3) & 0x0F);
		  
		return new Key(fieldID, firstByte & 0x07);
	}
	
	public static void writeBool(Stream stream, boolean val) throws Exception {
		if (val) {
			stream.writeByte(1);
		} else {
			stream.writeByte(0);
		}
	}
	
	public static void writeUInt32(Stream stream, int val) throws Exception {
		int b;
	  
		val = Math.abs(val);

		while (true) {
			b = val & 0x7F;
			val >>>= 7;
			if (val == 0) {
				stream.writeByte(b);
	      
				break;
			} else {
				b |= 0x80;
	     
				stream.writeByte(b);
			}
		}
	}
	
	public static void writeZInt32(Stream stream, int val) throws Exception {
		int val1 = val << 1;
		int val2 = val >> 31;
	
		writeUInt32(stream, val1 ^ val2);
	}
	
	public static void writeSingle(Stream stream, float val) throws Exception {
		int newVal = Float.floatToIntBits(val);
		
		stream.writeByte(newVal & 0xff);
		stream.writeByte((newVal >> 8) & 0xff);
		stream.writeByte((newVal >> 16) & 0xff);
		stream.writeByte((newVal >> 24) & 0xff);
	}
	
	public static void writeUInt64(Stream stream, long val) throws Exception {
	  	int b;
	  
	  	val = Math.abs(val);
	  
	  	while (true) {
	  		b = (int)(val & 0x7F);
		    val >>>= 7;
		    if (val == 0) {
		    	stream.writeByte(b);
		        
		    	break;
		    } else {
		    	b |= 0x80;
		        
		    	stream.writeByte(b);
		    }
	  	}
	}
	
	public static void writeZInt64(Stream stream, long val) throws Exception {
		boolean sign = false;
	  
		if (val < 0) { 
	    	val = -val;
	    
	    	sign = true;
		}
	  
	    val <<= 1;
	  
	    if (sign == true) {
	    	val ++;
	    }
	  
	    writeUInt64(stream, val);
	}
	
	public static void writeDouble(Stream stream, double val) throws Exception {
		long newVal = Double.doubleToLongBits(val);
		
		stream.writeByte((int)(newVal & 0xff));
		stream.writeByte((int)((newVal >> 8) & 0xff));
		stream.writeByte((int)((newVal >> 16) & 0xff));
		stream.writeByte((int)((newVal >> 24) & 0xff));
		stream.writeByte((int)((newVal >> 32) & 0xff));
		stream.writeByte((int)((newVal >> 40) & 0xff));
		stream.writeByte((int)((newVal >> 48) & 0xff));
		stream.writeByte((int)((newVal >> 56) & 0xff));
	}
	
	public static void writeBytes(Stream stream, List<Byte>val, int len) throws Exception {
		writeUInt32(stream, len);
		stream.writeBytes(val, 0, len);
	}
	
	public static void writeString(Stream stream, String val) throws Exception {
		byte[] tempBuffer = val.getBytes(StandardCharsets.ISO_8859_1);
	  	List<Byte> array = new ArrayList<>();
	  	
	  	for (byte b : tempBuffer) {
			array.add(b);
		}
	  
	  	writeBytes(stream, array, array.size());
	}
	
	public static boolean skipKey(Stream stream, Key key) throws Exception {
		if (key.wireType == Wire.FIXED32.getValue()) {
			stream.seek(4);
		} else if (key.wireType == Wire.FIXED64.getValue()) {
			stream.seek(8);
		} else if (key.wireType == Wire.LENGTH_DELIMITED.getValue()) {
			stream.seek(readUInt32(stream));
		} else if (key.wireType == Wire.VARINT.getValue()) {
			readSkipVarInt(stream);
		} else {
			System.err.println("WARNING: Unknown wire type: " + key.wireType);

			return false;
		}

	  	return true;
	}
}
