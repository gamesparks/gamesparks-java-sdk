package com.gamesparks.sdk.realtime;

class KeyValue {
	public Key key;
	public byte[] value;

	public KeyValue(Key key, byte[] value)
	{
		this.key = key;
		this.value = value;
	}

	public String toString()
	{
		return "[KeyValue: " + key.field + ", " + key.wireType + ", " + value.length + " bytes]";
	}
}
