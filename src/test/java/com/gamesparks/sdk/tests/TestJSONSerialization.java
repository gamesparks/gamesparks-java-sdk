package com.gamesparks.sdk.tests;

import static org.junit.Assert.*;

import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;

import com.gamesparks.sdk.api.*;
import com.gamesparks.sdk.Assert;

public class TestJSONSerialization {

	private final String longValue = "-1000000.00";
	private final long expectedLongValue = -1000000L;
	private final int expectedIntValue = -1000000;

	private final String numberValue = "-0.000001";
	private final float expectedFloatValue = -0.000001f;
	private final double expectedDoubleValue = -0.000001d;
    
	private final String stringKey = "stringKey";
	private final String stringValue = "stringValue";

	private final String floatKey = "floatKey";
	private final String doubleKey = "doubleKey";
	private final String intKey = "intKey";
	private final String longKey = "longKey";

    private GSData dataToTest;
    private JSONParser jsonParser = new JSONParser();
    
    @Before
    public void setupData() throws Exception {
    	String message = "{" +
    			"\"" + floatKey + "\" : " + numberValue + "," +
                "\"" + doubleKey + "\" : " + numberValue + "," +
                "\"" + stringKey + "\" : \"" + stringValue + "\"," +
                "\"" + longKey + "\" : " + longValue + "," +
                "\"" + intKey + "\" : " + longValue + "," +
            "}";
    	Map<String, Object> parsed = (Map<String, Object>)jsonParser.parse(message);
		
		jsonParser.reset();

        dataToTest = new GSData(parsed);
    }
	
	@Test
	public void numberParsing() throws Exception {
		System.out.println("*** NumberParsing ***");
		
		assertNotNull(dataToTest.getNumber(longKey));

		assertEquals(expectedLongValue, dataToTest.getNumber(longKey).longValue());
		
		System.out.println("Passed!");
	}

	@Test
	public void floatParsing() throws Exception {
		System.out.println("*** FloatParsing ***");
		
		assertNotNull(dataToTest.getFloat(floatKey));

		assertEquals(expectedFloatValue, dataToTest.getFloat(floatKey).floatValue(), 0.1f);
		
		System.out.println("Passed!");
	}
	
	@Test
	public void intParsing() throws Exception {
		System.out.println("*** IntParsing ***");
		
		assertNotNull(dataToTest.getInteger(intKey));

		assertEquals(expectedIntValue, dataToTest.getInteger(intKey).intValue());
		
		System.out.println("Passed!");
	}
	
	@Test
	public void longParsing() throws Exception {
		System.out.println("*** LongParsing ***");
		
		assertNotNull(dataToTest.getLong(longKey));

		assertEquals(expectedLongValue, dataToTest.getLong(longKey).longValue());
		
		System.out.println("Passed!");
	}
	
	@Test
	public void stringParsing() throws Exception {
		System.out.println("*** StringParsing ***");
		
		assertNotNull(dataToTest.getString(stringKey));

		assertEquals(stringValue, dataToTest.getString(stringKey));
		
		System.out.println("Passed!");
	}
}
