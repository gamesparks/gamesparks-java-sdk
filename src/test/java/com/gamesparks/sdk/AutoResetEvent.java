package com.gamesparks.sdk;

import static org.junit.Assert.fail;

public class AutoResetEvent {
	
	public interface Callback {
        void function(boolean timedout) throws Exception;
    }
	
	private class MyThread implements Runnable {
		
		private int mTimeout;
		private Callback mCallback;
		
		public MyThread(int timeout, Callback callback) {
			mTimeout = timeout;
			mCallback = callback;
		}

		public void run() {
			try {
				for (int i = mTimeout * 10; i > 0 && !mState; i--) {
					Thread.sleep(100);
				}
			
				if (mState) {
					mState = false;
					
					mCallback.function(false);
				} else {
					mCallback.function(true);
				}
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}
	
	private boolean mState;
	
	public AutoResetEvent(boolean initialState)
	{
		mState = initialState;
	}
	
	public void set()
	{
		mState = true;
	}
	
	public boolean isSet()
	{
		return mState;
	}

	public void waitOne(int timeout, Callback callback)
	{
		try {
			Runnable r = new MyThread(timeout, callback);
			new Thread(r).start();
		} catch (Exception e) {
		}
	}
}
