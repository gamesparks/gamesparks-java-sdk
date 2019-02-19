package com.gamesparks.sdk;

import com.gamesparks.sdk.AutoResetEvent.Callback;

public class CountdownLatch {
	public interface Callback {
        void function(boolean timedout);
    }
	
	private boolean complete;
	private int count;
	private int timeout;
	private long startTime;
	private Callback callback;
	
	public CountdownLatch(int initialCount) {
		complete = false;
		count = initialCount;
		timeout = 0;
	}
	
	public void signal() {
		if (count > 0) {
			count --;
			
			if (count == 0) {
				complete = true;
			}
		}
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public void wait(final int timeout, final Callback callback) {
		this.timeout = timeout;
		startTime = System.currentTimeMillis();
		this.callback = callback;
		
		//Thread thread = new Thread() {
			//public void run() {
				while (true) {
					if (System.currentTimeMillis() - startTime >= timeout * 1000 && count > 0) {
						callback.function(true);
						
						return;
					} else if (count == 0) {
						callback.function(false);
						
						return;
					}
						
					try {
						Thread.sleep(1000 / 60);
					} catch (InterruptedException e) {				
					}		
				}
			//}
		//};
		
		//thread.start();
	}
}
