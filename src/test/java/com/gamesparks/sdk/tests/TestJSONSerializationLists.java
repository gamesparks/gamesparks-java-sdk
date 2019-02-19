package com.gamesparks.sdk.tests;

import static org.junit.Assert.*;

import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;

import com.gamesparks.sdk.Assert;
import com.gamesparks.sdk.api.GSData;

public class TestJSONSerializationLists {
	
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

	private final int listCount = 2;

    private GSData dataToTest;
    private JSONParser jsonParser = new JSONParser();

    @Before
    public void setupData() throws Exception {
    	String message = "{" +
                "\"" + floatKey + "\" : [" + numberValue + "," + numberValue + "]" +
                "\"" + doubleKey + "\" : [" + numberValue + "," + numberValue + "]" +
                "\"" + stringKey + "\" : [\"" + stringValue + "\",\"" + stringValue + "\"]" +
                "\"" + longKey + "\" : [" + longValue + "," + longValue + "]" +
                "\"" + intKey + "\" : [" + longValue + "," + longValue + "]" +
            "}";
    	Map<String, Object> parsed = (Map<String, Object>)jsonParser.parse(message);
		
		jsonParser.reset();

        dataToTest = new GSData(parsed);
    }
    
    @Test
	public void numberParsing() throws Exception {
		System.out.println("*** NumberParsing ***");
		
		assertNotNull(dataToTest.getNumberList(longKey));

		assertEquals(listCount, dataToTest.getNumberList(longKey).size());
        assertEquals(expectedLongValue, dataToTest.getNumberList(longKey).get(0).longValue());
        assertEquals(expectedLongValue, dataToTest.getNumberList(longKey).get(1).longValue());
		
		System.out.println("Passed!");
	}

	@Test
	public void floatParsing() throws Exception {
		System.out.println("*** FloatParsing ***");
		
		assertNotNull(dataToTest.getNumberList(floatKey));

		assertEquals(listCount, dataToTest.getNumberList(floatKey).size());
        assertEquals(expectedFloatValue, dataToTest.getNumberList(floatKey).get(0).floatValue(), 0.1f);
        assertEquals(expectedFloatValue, dataToTest.getNumberList(floatKey).get(1).floatValue(), 0.1f);
        
		System.out.println("Passed!");
	}
	
	@Test
	public void intParsing() throws Exception {
		System.out.println("*** IntParsing ***");
		
		assertNotNull(dataToTest.getNumberList(intKey));

		assertEquals(listCount, dataToTest.getNumberList(intKey).size());
        assertEquals(expectedIntValue, dataToTest.getNumberList(intKey).get(0).intValue());
        assertEquals(expectedIntValue, dataToTest.getNumberList(intKey).get(1).intValue());
       
		System.out.println("Passed!");
	}
	
	@Test
	public void longParsing() throws Exception {
		System.out.println("*** LongParsing ***");
		
		assertNotNull(dataToTest.getNumberList(longKey));

		assertEquals(listCount, dataToTest.getNumberList(longKey).size());
        assertEquals(expectedLongValue, dataToTest.getNumberList(longKey).get(0).longValue());
        assertEquals(expectedLongValue, dataToTest.getNumberList(longKey).get(1).longValue());
       
		System.out.println("Passed!");
	}
	
	@Test
	public void stringParsing() throws Exception {
		System.out.println("*** StringParsing ***");
		
		assertNotNull(dataToTest.getStringList(stringKey));

		assertEquals(listCount, dataToTest.getStringList(stringKey).size());
        assertEquals(stringValue, dataToTest.getStringList(stringKey).get(0));
        assertEquals(stringValue, dataToTest.getStringList(stringKey).get(1));
       
		System.out.println("Passed!");
	}
}
