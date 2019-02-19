package com.gamesparks.sdk;

public class Assert {
	
	public static volatile Exception exc;
	
    public static void assertTrue(Object...args) throws Exception {
        String message;
        boolean condition;

        if(args.length == 1) {
            message = "";
            condition = (boolean)args[0];
        }
        else if(args.length == 2) {
            message = (String)args[0];
            condition = (boolean)args[1];
        }
        else {
            throw new Exception("Invalid argument count");
        }

        if(!condition) {
            fail(message);
        }
    }
   
    public static void assertFalse(Object...args) throws Exception {
    	String message;
        boolean condition;

        if(args.length == 1) {
            message = "";
            condition = (boolean)args[0];
        }
        else if(args.length == 2) {
            message = (String)args[0];
            condition = (boolean)args[1];
        }
        else {
            throw new Exception("Invalid argument count");
        }

        assertTrue(message, !condition);
    }
    
    public static void fail(String message) throws Exception {
    	System.out.println(message);
    	
    	exc = new Exception(message);
    	
    	throw exc;
    	
    	//System.exit(0);
    }

    /*public static void assertThrows(errorType:Class, block:Function) {
        try {
            block.call();
            fail("assertThrows block did not throw an expected exception");
        }
        catch(e:Error) {
            if(!(e is errorType)) {
                fail("assertThrows did not throw the expected error type, instead threw: " + getQualifiedClassName(e));
            }
        }
    }*/

    public static void assertEquals(Object...args) throws Exception {
        String message;
        Object expected;
        Object actual;

        if(args.length == 2) {
            message = "";
            expected = args[0];
            actual = args[1];
        }
        else if(args.length == 3) {
            message = (String)args[0];
            expected = args[1];
            actual = args[2];
        }
        else {
            throw new Exception("Invalid argument count");
        }

        if(expected == null && actual == null) {
            return;
        }

        try {
            if(expected != null && expected.equals(actual)) {
                return;
            }
        }
        catch(Exception e) {
            if(expected != null && expected == actual) {
                return;
            }
        }

        failNotEquals(message, expected, actual);
    }
   
    public static void assertNotNull(Object...args) throws Exception {
        String message;
        Object object;

        if(args.length == 1) {
            message = "";
            object = args[0];
        }
        else if(args.length == 2) {
            message = (String)args[0];
            object = args[1];
        }
        else {
            throw new Exception("Invalid argument count");
        }

        assertTrue(message, object != null);
    }
    
    public static void assertNull(Object...args) throws Exception {
        String message;
        Object object;

        if(args.length == 1) {
            message = "";
            object = args[0];
        }
        else if(args.length == 2) {
            message = (String)args[0];
            object = args[1];
        }
        else {
            throw new Exception("Invalid argument count");
        }

        assertTrue(message, object == null);
    }
    
    /*public static void assertSame(Object...args) {
        String message;
        Object expected;
        Object actual;

        if(args.length == 2) {
            message = "";
            expected = args[0];
            actual = args[1];
        }
        else if(args.length == 3) {
            message = (String)args[0];
            expected = args[1];
            actual = args[2];
        }
        else {
            throw new Exception("Invalid argument count");
        }

        if(expected === actual) {
            return;
        }
        failNotSame(message, expected, actual);
    }
    
    public static void assertNotSame(...args:Array) {
        var message:String;
        var expected:Object;
        var actual:Object;

        if(args.length == 2) {
            message = "";
            expected = args[0];
            actual = args[1];
        }
        else if(args.length == 3) {
            message = args[0];
            expected = args[1];
            actual = args[2];
        }
        else {
            throw new IllegalOperationError("Invalid argument count");
        }

        if(expected === actual)
            failSame(message);
    }*/

    public static void assertEqualsFloat(Object...args) throws Exception {
        String message;
        double expected;
        double actual;
        double tolerance = 0;

        if(args.length == 3) {
            message = "";
            expected = (double)args[0];
            actual = (double)args[1];
            tolerance = (double)args[2];
        }
        else if(args.length == 4) {
            message = (String)args[0];
            expected = (double)args[1];
            actual = (double)args[2];
            tolerance = (double)args[3];
        }
        else {
            throw new Exception("Invalid argument count");
        }
        if (Double.isNaN(tolerance)) tolerance = 0;
        if(Math.abs(expected - actual) <= tolerance) {
               return;
        }
        failNotEquals(message, expected, actual);
    }

    public static void assertEqualsArrays(Object...args) throws Exception {
        String message;
        Object[] expected;
        Object[] actual;

        if(args.length == 2) {
            message = "";
            expected = (Object[])args[0];
            actual = (Object[])args[1];
        }
        else if(args.length == 3) {
            message = (String)args[0];
            expected = (Object[])args[1];
            actual = (Object[])args[2];
        }
        else {
            throw new Exception("Invalid argument count");
        }
        
        if (expected == null && actual == null) {
            return;
        }
        if ((expected == null && actual != null) || (expected != null && actual == null)) {
            failNotEquals(message, expected, actual);
        }
        // from here on: expected != null && actual != null
        if (expected.length != actual.length) {
            failNotEquals(message, expected, actual);
        }
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    public static void assertEqualsArraysIgnoringOrder(Object...args) throws Exception {
    	String message;
        Object[] expected;
        Object[] actual;

        if(args.length == 2) {
            message = "";
            expected = (Object[])args[0];
            actual = (Object[])args[1];
        }
        else if(args.length == 3) {
            message = (String)args[0];
            expected = (Object[])args[1];
            actual = (Object[])args[2];
        }
        else {
            throw new Exception("Invalid argument count");
        }

        if (expected == null && actual == null) {
            return;
        }
        if ((expected == null && actual != null) || (expected != null && actual == null)) {
            failNotEquals(message, expected, actual);
        }
        // from here on: expected != null && actual != null
        if (expected.length != actual.length) {
            failNotEquals(message, expected, actual);
        }
        for (int i = 0; i < expected.length; i++) {
            boolean foundMatch = false;
            Object expectedMember = expected[i];
            for (int j = 0; j < actual.length; j++) {
                Object actualMember = actual[j];
                try {
                    assertEquals(expectedMember, actualMember);
                    foundMatch = true;
                    break;
                }
                catch (Exception e) {
                    //  no match, try next
                }
            }
            if (!foundMatch) {
                failNotEquals("Found no match for " + expectedMember + ";", expected, actual);
            }
        }
    }

    private static void failSame(String message) throws Exception {
        String formatted = "";
         if(message != null) {
             formatted = message + " ";
         }
         fail(formatted + "expected not same");
    }

    private static void failNotSame(String message, Object expected, Object actual) throws Exception {
        String formatted = "";
        if(message != null) {
            formatted = message + " ";
        }
        fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
    }

    private static void failNotEquals(String message, Object expected, Object actual) throws Exception {
        fail(format(message, expected, actual));
    }

    private static String format(String message, Object expected, Object actual) {
        String formatted = "";
        if(message != null) {
            formatted = message + " ";
        }
        return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
    }
}
