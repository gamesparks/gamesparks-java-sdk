package com.gamesparks.sdk.realtime;

import java.util.*;

class ObjectPool<T> {
	private int maxSize = 5;
	private ObjectPoolCreatorEvent<T> creator = null;
	private ObjectPoolRefresherEvent<T> refresher = null;
	private Stack<T> stack = new Stack<T>();
	
	public ObjectPool(ObjectPoolCreatorEvent<T> creator){
		this.creator = creator;
	}
	
	public ObjectPool(ObjectPoolCreatorEvent<T> creator, ObjectPoolRefresherEvent<T> refresher) {
		this.creator = creator;
		this.refresher = refresher;
	}

	public ObjectPool(ObjectPoolCreatorEvent<T> creator, int maxSize) {
		this.creator = creator;
		this.maxSize = maxSize;
	}

	public ObjectPool(ObjectPoolCreatorEvent<T> creator, ObjectPoolRefresherEvent<T> refresher, int maxSize) {
		this.creator = creator;
		this.refresher = refresher;
		this.maxSize = maxSize;
	}

	public T pop()
	{
		synchronized (stack)
		{
			if (stack.isEmpty()) {
				return creator.onEvent();
			} else {
				return stack.pop();
			}
		}
	}

	public void push(T item)
	{
		if (item != null) {
			synchronized (stack) {
				if (stack.contains(item)) {
					return;
				}
				if (stack.size() < maxSize) {
					if (refresher != null) {
						refresher.onEvent(item);
					}
					stack.push(item);
				}
			}
		}
	}
	
	public void dispose()
	{
		synchronized (stack)
		{
			stack.clear();
		}
	}
}
