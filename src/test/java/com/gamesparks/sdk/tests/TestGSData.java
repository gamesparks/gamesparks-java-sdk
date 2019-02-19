package com.gamesparks.sdk.tests;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.gamesparks.sdk.Assert;
import com.gamesparks.sdk.api.GSData;

public class TestGSData {

	@Test
	public void checkValues() throws Exception {
		System.out.println("*** CheckValues ***");
		
		GSData rd = new GSData();
		Map<String,Object> data = rd.getBaseData();
		List<Integer> iList = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9 );
		List<Long> lList = Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
		List<Float> fList = Arrays.asList(0.0F, 1.0F, 2.0F, 3.0F, 4.0F, 5.0F, 6.0F, 7.0F, 8.0F, 9.0F);
		List<Double> dList = Arrays.asList(0.0D, 1.0D, 2.0D, 3.0D, 4.0D, 5.0D, 6.0D, 7.0D, 8.0D, 9.0D);
		List<String> sList = Arrays.asList("a", "b", "c", "d", "e", "f");
		
		data.put ("boolean", true);
		data.put ("int", 1);
		data.put ("long", 2L);
		data.put ("float", 3.0F);
		data.put ("double", 4.0D);
		data.put ("string", "test");
		data.put ("int list", iList);
		data.put ("long list", lList);
		data.put ("float list", fList);
		data.put ("double list", dList);
		data.put ("string list", sList);
		
		Boolean bValue = rd.getBoolean("boolean");
		assertTrue(bValue != null);
		assertTrue(bValue.booleanValue() == true);
		
		Integer iValue = rd.getInteger("int");
		assertTrue(iValue != null);
		assertTrue(iValue.intValue() == 1);

		Number nValue = rd.getNumber("int");
		assertTrue(nValue != null);
		assertTrue(nValue.intValue() == 1);

		Long lValue = rd.getLong("long");
		assertTrue(lValue != null);
		assertTrue(lValue.longValue() == 2);

		nValue = rd.getNumber("long");
		assertTrue(nValue != null);
		assertTrue(nValue.longValue() == 2);

		Float fValue = rd.getFloat("float");
		assertTrue(fValue != null);
		assertTrue(fValue.floatValue() == 3.0f);

		nValue = rd.getNumber("float");
		assertTrue(nValue != null);
		assertTrue(nValue.longValue() == 3);

		Double dValue = rd.getDouble("double");
		assertTrue(dValue != null);
		assertTrue(dValue.doubleValue() == 4.0d);

		nValue = rd.getNumber("double");
		assertTrue(nValue != null);
		assertTrue(nValue.longValue() == 4);

		String sValue = rd.getString("string");
		assertTrue(sValue != null && sValue.length() > 0);
		assertTrue(sValue.compareTo("test") == 0);
		
		List<Number> newIList = rd.getNumberList("int list");
		assertTrue(newIList != null);
		newIList.removeAll((Collection<?>)iList);
		assertTrue(newIList.size() == 0);

		List<Number> newLList = rd.getNumberList("long list");
		assertTrue(newLList != null);
		newLList.removeAll((Collection<?>)lList);
		assertTrue(newLList.size() == 0);

		List<Number> newFList = rd.getNumberList("float list");
		assertTrue(newFList != null);
		newFList.removeAll((Collection<?>)fList);
		assertTrue(newFList.size() == 0);

		List<Number> newDList = rd.getNumberList("double list");
		assertTrue(newDList != null);
		newDList.removeAll((Collection<?>)dList);
		assertTrue(newDList.size() == 0);

		List<String> newSList = rd.getStringList("string list");
		assertTrue(newSList != null);
		newSList.removeAll((Collection<?>)sList);
		assertTrue(newSList.size() == 0);
		
		System.out.println("Passed!");
	}

}
