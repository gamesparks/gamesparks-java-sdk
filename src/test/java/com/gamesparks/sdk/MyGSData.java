package com.gamesparks.sdk;

import java.util.List;

import com.gamesparks.sdk.api.GSData;

public class MyGSData extends GSData {
	public MyGSData(GSData data) {
		super(data);
	}
	
	public List<String> getMyStringList(String name){
		return getStringList(name);
	}

	public List<Number> getMyNumberList(String name){
		return getNumberList(name);
	}
	
	public List<Boolean> getMyBooleanList(String name){
		return getBooleanList(name);
	}
}
