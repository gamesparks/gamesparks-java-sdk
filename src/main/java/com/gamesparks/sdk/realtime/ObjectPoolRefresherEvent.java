package com.gamesparks.sdk.realtime;

interface ObjectPoolRefresherEvent<T> {
	public void onEvent(T item);
}
