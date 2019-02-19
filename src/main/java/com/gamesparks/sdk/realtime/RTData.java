package com.gamesparks.sdk.realtime;

public class RTData {
	RTVal[] data;
	
	public RTData() {
		data = new RTVal[GameSparksRT.MAX_RTDATA_SLOTS];
		
		for (int i = 0; i < GameSparksRT.MAX_RTDATA_SLOTS; i ++) {
			data[i] = new RTVal();
		}
	}
	
	public static RTData get() {
		return RTDataSerializer.cache.pop();
	}

	static RTData readRTData(Stream stream, RTData instance) throws Exception {
		return RTDataSerializer.readRTData(stream, instance);
	}

	static void writeRTData(Stream stream, RTData instance) throws Exception {
		RTDataSerializer.writeRTData(stream, instance);
	}
	
	public void dispose() {

		for (int i = 0; i < data.length; i++)
		{
			if (data[i].dirty())
			{
				data[i] = new RTVal();
			}
		}

		RTDataSerializer.cache.push(this);
	}
	
	public Integer getInt(int index) {
		if (data[index].longVal != null) {
			return data[index].longVal.intValue();
		}
		
		return null;
	}

	public RTVector getRTVector(int index) {
		return data[index].vecVal;
	}

	public Long getLong(int index) {
		return data[index].longVal;
	}

	public Float getFloat(int index) {
		return data[index].floatVal;
	}

	public Double getDouble(int index) {
		return data[index].doubleVal;
	}

	public String getString(int index) {
		return data[index].stringVal;
	}

	public RTData getData(int index) {
		return data[index].dataVal;
	}

	public RTData setInt(int index, int value) {
		data[index] = new RTVal((long)value);
		
		return this;
	}

	public RTData setLong(int index, long value) {
		data[index] = new RTVal(value);
		
		return this;
	}

	public RTData setRTVector(int index, RTVector value) {
		data[index] = new RTVal(value);
		
		return this;
	}

	public RTData setFloat(int index, float value) {
		data[index] = new RTVal(value);
		
		return this;
	}

	public RTData setDouble(int index, double value) {
		data[index] = new RTVal(value);
		
		return this;
	}

	public RTData setString(int index, String value) {
		data[index] = new RTVal(value);
		
		return this;
	}

	public RTData setData(int index, RTData value) {
		data[index] = new RTVal(value);
		
		return this;
	}

	public String toString() {
		return asString();
	}

	protected String asString() {
		StringBuilder builder = new StringBuilder(" {");
		
		for (int i = 0; i < GameSparksRT.MAX_RTDATA_SLOTS; i++) {
			String val = data[i].asString();
			
			if (val != null) {
				builder.append(" [" + i + " " + val + "] ");
			}
		}
		builder.append("} ");
		
		return builder.toString();
	}
}
