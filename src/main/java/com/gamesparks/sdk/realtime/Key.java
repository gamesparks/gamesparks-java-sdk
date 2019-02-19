package com.gamesparks.sdk.realtime;

class Key {
	public enum Wire {
		VARINT(0),
		FIXED64(1),
		LENGTH_DELIMITED(2),
		FIXED32(5);
	
		private final int value;
		
	    Wire(int value) { 
	    	this.value = value;
	    }
	    
	    public int getValue() { 
	    	return value;
	    }
	}
	
	public int field;
	public int wireType;
	
	public Key(int field, int wireType)
	{
		this.field = field;
		this.wireType = wireType;
	}

	public Key(int field, Wire wireType)
	{
		this.field = field;
		this.wireType = wireType.getValue();
	}

	public String toString()
	{
		return "[Key: " + field + ", " + wireType + "]";
	}
}
