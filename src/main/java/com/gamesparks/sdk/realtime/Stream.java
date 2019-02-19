package com.gamesparks.sdk.realtime;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Stream {
	private List<Byte> buffer;
	private int position;
	
	public Stream() {
		buffer = new ArrayList<>();
		position = 0;
	}
	
	public int getPosition() {
	  return position;
	}

	public void setPosition(int position) {
	  this.position = position;
	}

	public int getLength() {
	  return buffer.size();
	}

	public int bytesAvailable() {
	  return buffer.size() - position;
	}
	
	public List<Object> readChars(String buffer, int position, int length) throws Exception {
		if (buffer == null) {
			throw new Exception("Buffer must be string!");
		}
		
		if (bytesAvailable() <= 0) {
			System.err.println("WARNING: Reached end of the stream");
			
			return new ArrayList<Object>(Arrays.asList(0, buffer));
		}
		
		if (length <= 0) {
			System.err.println("WARNING: No characters read (length = 0)");
			
			return new ArrayList<Object>(Arrays.asList(0, buffer));
		}
		
		if (buffer.length() > 0) {
			if (position < 0 || position >= buffer.length()) {
				throw new Exception("Position out of range!");
			}
		} else {
	    	position = 0;
		}
		
		int startPosition = this.position + 1;
		int endPosition = startPosition + length - 1;
		int newLength;
		List<Byte> subBuffer;
		List<Byte> newBuffer = new ArrayList<>();
		
		if (endPosition > this.buffer.size()) {
			endPosition = this.buffer.size();
		}
		
		newLength = endPosition - startPosition + 1;
		
		subBuffer = this.buffer.subList(startPosition - 1, endPosition);
		
		this.position = endPosition;
		
		if (position == 0) {
			newBuffer.addAll(subBuffer);
			
			if (subBuffer.size() < buffer.length()) {
				byte[] tempBuffer = buffer.substring(newLength).getBytes(StandardCharsets.ISO_8859_1);
				
				for (byte b : tempBuffer) {
					newBuffer.add(b);
				}
			}
		} else {
			byte[] tempBuffer = buffer.substring(0, position).getBytes(StandardCharsets.ISO_8859_1);
			
			for (byte b : tempBuffer) {
				newBuffer.add(b);
			}
			
			newBuffer.addAll(subBuffer);
			
			if (position + newLength + 1 <= buffer.length()) {
				tempBuffer = buffer.substring(position + newLength).getBytes(StandardCharsets.ISO_8859_1);
				
				for (byte b : tempBuffer) {
					newBuffer.add(b);
				}
			}
		}
		
		return new ArrayList<>(Arrays.asList(newLength, newBuffer));
	}
	
	/*public Object readChar() throws Exception {
		int ret = readByte();
		
		if (ret >= 0) {
			return Character.valueOf((char)ret);
		} else {
			return -1;
		}
	}*/
	
	@SuppressWarnings("unchecked")
	public int readByte() throws Exception {
	  	int totalBytes;
		List<Byte> buffer;
		List<Object> ret = readChars("", position, 1);
		
		totalBytes = (int)(ret.get(0));
		buffer = (List<Byte>)(ret.get(1));
		
		if (totalBytes == 1) {
			return (int)buffer.get(0) & 0xff;
		} else {
			return -1;
		}
	}
	
	public void writeChars(byte[] buffer, int position, int length) throws Exception {
		if (buffer == null || buffer.length <= 0) {
			throw new Exception("Buffer must not be empty!");
		}
		
		if (bytesAvailable() < 0) {
			System.err.println("WARNING: Reached end of the stream");
			
			return;
		}
		
		if (length <= 0) {
			System.err.println("WARNING: No characters written (length = 0)");
			
			return;
		}
		
		if (position < 0 || position >= buffer.length) {
			throw new Exception("Position out of range!");
		}
		
		int startPosition = position + 1;
		int endPosition = startPosition + length - 1;
		int newLength;
		List<Byte> subBuffer = new ArrayList<>();
		List<Byte> newBuffer = new ArrayList<>();
		
		if (endPosition > buffer.length) {
			endPosition = buffer.length;
		}
		
		newLength = endPosition - startPosition + 1;
		
		byte[] tempBuffer = Arrays.copyOfRange(buffer, startPosition - 1, endPosition);
		
		for (byte b : tempBuffer) {
			subBuffer.add(b);
		}
		
		if (this.position == 0) {
			newBuffer.addAll(subBuffer);
			
			if (this.buffer.size() > subBuffer.size()) {
				newBuffer.addAll(this.buffer.subList(newLength, this.buffer.size()));
			}
		} else {
			newBuffer.addAll(this.buffer.subList(0, this.position));
			newBuffer.addAll(subBuffer);
			
			if (this.position + newLength + 1 <= this.buffer.size()) {
				newBuffer.addAll(this.buffer.subList(this.position + newLength, this.buffer.size()));
			}
		}
		
		this.buffer = newBuffer;
		this.position += newLength;
	}
	
	public void writeChar(char c) throws Exception {
		writeChars(new byte[] {(byte)(c & 0xff)}, 0, 1);
	}
	
	public void writeByte(int b) throws Exception {
		if (b < 0 || b > 255) {
			throw new Exception("Not valid byte!");
		}
	 
		writeChar((char)b);
	}
	
	@SuppressWarnings("unchecked")
	public int read(List<Byte> buffer, int position, int length) throws Exception {
		int totalBytes;
		List<Byte> newBuffer;
		List<Object> ret;
		StringBuilder builder = new StringBuilder(buffer.size());
		
	    for (Byte ch: buffer)
	    {
	        builder.append(ch);
	    }
	    
	    ret = readChars(builder.toString(), position, length);
	    
	    totalBytes = (int)(ret.get(0));
		newBuffer = (List<Byte>)(ret.get(1));
	    
	    buffer.clear();
	    
	    buffer.addAll(newBuffer);
		
		return totalBytes;
	}
	
	public void writeBytes(List<Byte> buffer, int position, int length) throws Exception {
		byte[] newBuffer = new byte[length];
		
		for (int i = position; i < position + length; i ++) {
			newBuffer[i - position] = buffer.get(i);
		}
		
		writeChars(newBuffer, 0, length);
	}
	
	public String toHex() {
		int end;
		StringBuffer output = new StringBuffer();
		
		for (int i = 1; i <= (int)Math.ceil((float)buffer.size() / 16.0f) * 16; i ++) {
			if ((i - 1) % 16 == 0) {
				output.append(String.format("%08X  ", i - 1));
			}
			if (i > buffer.size()) {
				output.append("   ");
			} else {
				output.append(String.format("%02X ", (int)(buffer.get(i - 1) & 0xff)));
			}
			if (i % 8 == 0) { 
				output.append(" "); 
			}
			if (i % 16 == 0) {
				if (i > buffer.size()) {
					end = buffer.size();
				} else {
					end = i;
				}
				
				List<Byte> subBuffer = buffer.subList(i - 16, end);
				StringBuilder newString = new StringBuilder();
				
				for (byte b : subBuffer) {
					newString.append((char)(b & 0xff));
				}
				
				output.append(newString.toString().replaceAll("[^\u0020-\u007E]", ".")).append('\n');
			}
		}
		
		return output.toString();
	}
	
	public int seek(int offset) throws Exception {
		position -= offset;
	  
		if (position < 0) {
			position = 0;
		} else if (position > buffer.size()) {
			position = buffer.size();
		}
		
		return position;
	}
	
	public String toString() {
		return new String(getBytes(), StandardCharsets.UTF_8);
	}
	
	public String toString(int offset, int length) {
		return new String(getBytes(), offset, length, StandardCharsets.UTF_8);
	}

	public List<Byte> getBuffer() {
		return buffer;
	}
	
	public byte[] getBytes() {
		byte[] newBuffer = new byte[buffer.size()];
		
		for (int i = 0; i < buffer.size(); i ++) {
			newBuffer[i] = buffer.get(i);
		}
		
		return newBuffer;
	}
	
	public byte[] getBytes(int length) {
		byte[] newBuffer = new byte[length];
		
		if (length > buffer.size()) {
			length = buffer.size();
		}
		
		for (int i = 0; i < length; i ++) {
			newBuffer[i] = buffer.get(i);
		}
		
		return newBuffer;
	}
}
