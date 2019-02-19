package com.gamesparks.sdk;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.gamesparks.sdk.GS.QueuedRequest;

public class GSExtended extends GS {
	
	private static boolean networkAvailable = true;
	
	private boolean disconnected = false;
	
	public GSExtended() {		
		super("exampleKey12", "exampleSecret1234567890123456789", "", true, true, new TestPlatform());
	}
	
	public void connect() {
		disconnected = false;
		
		start();
	}
	
	public void disconnect() {
		if (disconnected)
		{
			return;
		}
		else
		{
			disconnected = true;
		}
		
		stop();
		
		/*synchronized(connections)
		{
			for (GSConnection connection : connections) {
				connection.close();
				
				connection.pendingQueue.clear();
	        }
		}
		
		currentUrl = serviceUrl;
		
		available = false;
	    authenticated = false;*/
	}
	
	public void closeAllSockets() {
		synchronized(connections)
		{
			for (GSConnection connection : connections) {
				connection.close();
	        }
		}
	}
	
	public void setNetworkAvailable(boolean available) {
		networkAvailable = available;
	}
	
	public int getSizePersistentQueue() {
		return persistentQueue.size();
	}
	
	public void clearDurableQueue() {
		persistentQueue.clear();
		
		durableQueueDirty = true;
		
		resetPersistentQueue();
	}
	
	public void setDurableQueuePaused(boolean pause)
	{
		durableQueuePaused = pause;
	}
	
	public boolean compareAttributeDurableRequest(int index, String attributeName, Object attributeValue)
	{
		QueuedRequest request = persistentQueue.get(index);
		
		if (request != null && request.request.getAttribute(attributeName).equals(attributeValue))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void removeDurableRequest(int index)
	{
		persistentQueue.remove(index);
	}
	
	public void setCallbackDurableRequest(int index, Object callback)
	{
		QueuedRequest request = persistentQueue.get(index);
		
		if (request != null)
		{
			request.request.setResponseHandler(callback);
		}
	}
	
	public int getRequestQueueCount() {
		return sendQueue.size();
	}
	
	protected void NewConnection()
    {
		if (networkAvailable) {
	    	synchronized (connections)
	        {
	    		try {
		    		GSConnectionExtended connection = new GSConnectionExtended(this);
		    		
		    		/*connection.pendingQueue.putAll(oldPendingQueue);
		    		
		    		for (Map.Entry<String, QueuedRequest> entry : connection.pendingQueue.entrySet()) {
		    			entry.getValue().waitForResponseTime = System.currentTimeMillis() + (entry.getValue().request.getResponseTimeout() * 1000);
		    		}
		    		
		    		oldPendingQueue.clear();*/
		    		
		            connections.addFirst(connection);
	    		} catch (Exception e) {
	    			stop();
	    		}
	        }
		}
    }
	
	protected void ProcessPersistentQueue()
	{
		Iterator<QueuedRequest> persistentQueueIterator = persistentQueue.iterator();
		
		writeDurableQueueIfDirty();
		
		if (!durableQueuePaused && networkAvailable && authenticated)
		{
			while (persistentQueueIterator.hasNext()) 
			{
        		QueuedRequest persistentRequest = persistentQueueIterator.next();
                
                if (connections.size() > 0 && connections.get(0).isReady()) {
	                if (persistentRequest.durableRetryTicks == 0 || persistentRequest.durableRetryTicks < System.currentTimeMillis()) {
	                	persistentRequest.durableRetryTicks = System.currentTimeMillis() + 10000;
	                	
	                	persistentRequest.cancelTime = (persistentRequest.request.getResponseTimeout() * 1000) + System.currentTimeMillis();
	                	persistentRequest.request.getBaseData().put("requestId", "d_" + System.currentTimeMillis() + "_" + ++currentRequestPrefix);
	               
	        			synchronized (sendQueue)
	        			{
	        	            sendQueue.addLast(persistentRequest);
	        	        }  
	                }
                } else {
                	return;
                }   
	        }
		}
	}
	
	protected void ProcessSendQueue()
    {
        while (sendQueue.size() > 0)
        {
        	synchronized (sendQueue)
            {
                QueuedRequest queuedRequest = sendQueue.getFirst();
                
                if (queuedRequest.cancelTime > 0 && queuedRequest.cancelTime < System.currentTimeMillis()){
                	sendQueue.remove(queuedRequest);
                    cancelRequest(queuedRequest.request);
                    continue;
                }

                if (connections.size() > 0 && connections.get(0).isReady()){
                	if (networkAvailable) {
                		connections.get(0).send(queuedRequest);
                	} else {
                		((GSConnectionExtended)connections.get(0)).sendFake(queuedRequest);
                	} 	
                	queuedRequest.waitForResponseTime = System.currentTimeMillis() + (queuedRequest.request.getResponseTimeout() * 1000);
                    sendQueue.remove(queuedRequest);
                } else {
                	return;
                }
            }
        }
    }
}
