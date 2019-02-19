package com.gamesparks.sdk.realtime;

import java.util.*;

import com.gamesparks.sdk.realtime.CommandFactory.OpCodes;

public class RTSession {
	String connectToken;
	private int tcpPort;
	int fastPort;
	private String hostName;
	List<Integer> activePeers;
	private boolean running;
	private boolean ready;
	private LinkedList<IRTCommand> actionQueue;
	GameSparksRT.ConnectState connectState;
	private int connectionAttempts;
	private long mustConnnectBy;
	private ReliableConnection reliableConnection;
	private FastConnection fastConnection;
	private int sequenceNumber;
	Integer peerId;
	private Map<Integer, Integer> peerMaxSequenceNumbers;
	IRTSessionListener listener;
	
	public RTSession(String connectToken, String hostName, int tcpPort, int udpPort, IRTSessionListener listener) {	
		this.connectToken = connectToken;
		this.tcpPort = tcpPort;
		this.fastPort = udpPort;
		this.hostName = hostName;
		this.listener = listener;
		
		activePeers = new ArrayList<>();
		running = false;
		ready = false;
		actionQueue = new LinkedList<IRTCommand>();
		connectState = GameSparksRT.ConnectState.DISCONNECTED;
		connectionAttempts = 1;
		mustConnnectBy = System.currentTimeMillis();
		reliableConnection = null;
		fastConnection = null;
		sequenceNumber = 0;
		peerId = null;
		peerMaxSequenceNumbers = new HashMap<Integer, Integer>();
	}
	
	public void start() {
		running = true;
	}

	public void stop() {
		log("IRTSession", GameSparksRT.LogLevel.DEBUG, "Stopped");

		running = false;
		ready = false;
	  
		if (fastConnection != null) {
			fastConnection.shutdown();
			fastConnection = null;
		}

		if (reliableConnection != null) {
			reliableConnection.shutdown();
			reliableConnection = null;
		}
	  
		setConnectState(GameSparksRT.ConnectState.DISCONNECTED);
	}
	
	public Integer getPeerId() {
		return peerId;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public List<Integer> getActivePeers() {
		return activePeers;
	}
	
	public GameSparksRT.ConnectState getConnectState() {
		return connectState;
	}
	
	public void update() {
		if (running) {
			checkConnection();
		}

		IRTCommand toExecute = null;
	  
		while (true) {
			toExecute = getNextAction();
			if (toExecute != null) {
				toExecute.execute();
			} else {
				break;
			}
		}
	}
	
	private IRTCommand getNextAction() {
		synchronized (actionQueue) {
			if (actionQueue.size() > 0) {      
				return actionQueue.pollFirst();
			}
		}
	  
	  	return null;
	}

	void submitAction(IRTCommand action) { 
		synchronized (actionQueue) {
			actionQueue.addLast(action);
		}
	}
	
	private void checkConnection() {
		if (System.currentTimeMillis() > mustConnnectBy) {
	    	if (connectState == GameSparksRT.ConnectState.DISCONNECTED) {
	    		log("IRTSession", GameSparksRT.LogLevel.INFO, "Disconnected, trying to connect");
	      
	    		setConnectState(GameSparksRT.ConnectState.CONNECTING);
	                
	    		connectReliable();

	    		connectionAttempts ++;
	    	} else if (connectState == GameSparksRT.ConnectState.CONNECTING) {
	    		setConnectState(GameSparksRT.ConnectState.DISCONNECTED);
	      
	    		log("IRTSession", GameSparksRT.LogLevel.INFO, "Not connected in time, retrying");

	    		if (reliableConnection != null) {
	    			reliableConnection.shutdown();
	    			reliableConnection = null;
	    		}
	    		if (fastConnection != null) {
	    			fastConnection.shutdown();
	    			fastConnection = null;
	    		}
	    	}
		}
	}
	
	void setConnectState(GameSparksRT.ConnectState value) {
		if (value != connectState) {
			if (connectState.getValue() < value.getValue() || value == GameSparksRT.ConnectState.DISCONNECTED) {
				log("IRTSession", GameSparksRT.LogLevel.DEBUG, "State Change : from " + connectState + " to " + value + ", ActivePeers " + activePeers.size());
	      
				connectState = value;

				if (connectState.getValue() >= GameSparksRT.ConnectState.RELIABLE_ONLY.getValue()) {
					mustConnnectBy = System.currentTimeMillis();
					connectionAttempts = 0;
				}
			}
		}
	}
	
	void connectFast() {
		log("IRTSession", GameSparksRT.LogLevel.DEBUG, peerId + ": Creating new fastConnection to " + fastPort);
	  
		fastConnection = new FastConnection(this);
		fastConnection.start(hostName, fastPort);
	}

	private void connectReliable() {
		mustConnnectBy = System.currentTimeMillis() + GameSparksRT.computeSleepPeriod(connectionAttempts) + GameSparksRT.handshakeOffset;
	  
		reliableConnection = new ReliableConnection(hostName, tcpPort, this);
	}

	int nextSequenceNumber() {
		int sequenceNumber = this.sequenceNumber;
	  
		this.sequenceNumber ++;
	  
	  	return sequenceNumber;
	}
	
	boolean shouldExecute(int peerId, Integer sequence) {
		if (sequence == null) {
			return true;
		} else if (peerMaxSequenceNumbers.get(peerId) == null) {
			peerMaxSequenceNumbers.put(peerId, 0);
		}

		if (peerMaxSequenceNumbers.get(peerId) > sequence) {
			log("IRTSession", GameSparksRT.LogLevel.DEBUG, "Discarding sequence id " + sequence + " from peer " + peerId);
	   
			return false;
		} else {
			peerMaxSequenceNumbers.put(peerId, sequence);
	    
			return true;
		}
	}
	
	private void resetSequenceForPeer(int peerId) {
		if (peerMaxSequenceNumbers.get(peerId) != null) {
			peerMaxSequenceNumbers.put(peerId, 0);
		}
	}

	void onPlayerConnect(int peerId) {
		resetSequenceForPeer(peerId);
		if (listener != null) {
			if (ready) {
				listener.onPlayerConnect(peerId);
			}
		}
	}

	void onPlayerDisconnect(int peerId) {
		if (listener != null) {
			if (ready) {
				listener.onPlayerDisconnect(peerId);
			}
		}
	}
	
	void onReady(final boolean ready) throws Exception {
		if (!this.ready && ready) {            
			sendData(OpCodes.PLAYER_READY_MESSAGE.getValue(), GameSparksRT.DeliveryIntent.RELIABLE, null, null);            
			if (peerId != null) {
				boolean ok = false;
				
				for (Iterator<Integer> iter = activePeers.iterator(); iter.hasNext(); ) {
					Integer element = iter.next();
					
					if (element.intValue() == peerId.intValue()) {
						ok = true;
						
						break;
					}
				}
				
				if (!ok) {
					activePeers.add(peerId);
				}
			}
		}

		this.ready = ready;

		if (!this.ready) {
			setConnectState(GameSparksRT.ConnectState.DISCONNECTED);
		}

		if (listener != null) {
			submitAction(ActionCommand.pool.pop().configure(new Action() {
				@Override
				public void onEvent() {
					listener.onReady(ready);
				}
			}));
		}
	}

	void onPacket(RTPacket packet) {
		if (listener != null) {
			listener.onPacket(packet);
		} else {
			System.err.println("AccessViolationException");
		}
	}
	
	public int sendData(int opCode, GameSparksRT.DeliveryIntent intent, byte[] payload, RTData data) throws Exception {
		return sendRTDataAndBytes(opCode, intent, payload, data, null);
	}
	
	public int sendData(int opCode, GameSparksRT.DeliveryIntent intent, byte[] payload, RTData data, int[] targetPlayers) throws Exception {
		return sendRTDataAndBytes(opCode, intent, payload, data, targetPlayers);
	}

	public int sendRTData(int opCode, GameSparksRT.DeliveryIntent intent, RTData data, int[] targetPlayers) throws Exception {
		return sendRTDataAndBytes(opCode, intent, null, data, targetPlayers);
	}

	public int sendBytes(int opCode, GameSparksRT.DeliveryIntent intent, byte[] payload, int[] targetPlayers) throws Exception {
		return sendRTDataAndBytes(opCode, intent, payload, null, targetPlayers);
	}
	
	public int sendRTDataAndBytes(int opCode, GameSparksRT.DeliveryIntent intent, byte[] payload, RTData data, int[] targetPlayers) throws Exception {
		int ret;
		CustomRequest csr = PooledObjects.customRequestPool.pop();
		List<Byte> newPayload = null;
		List<Integer> newTargetPlayers = null;
		
		if (payload != null && payload.length > 0) {
			newPayload = new ArrayList<>();
			
			for (int i = 0; i < payload.length; i ++) {
				newPayload.add(payload[i]);
			}
		}
		
		if (targetPlayers != null && targetPlayers.length > 0) {
			newTargetPlayers = new ArrayList<>();
			
			for (int i = 0; i < targetPlayers.length; i ++) {
				newTargetPlayers.add(targetPlayers[i]);
			}
		}
	  
		csr.configure(opCode, intent, newPayload, data, newTargetPlayers);
		if (intent != GameSparksRT.DeliveryIntent.RELIABLE && connectState.getValue() >= GameSparksRT.ConnectState.RELIABLE_AND_FAST_SEND.getValue()) {
			ret = fastConnection.send(csr);
	    
			PooledObjects.customRequestPool.push(csr);
	    
			return ret;
		} else if (connectState.getValue() >= GameSparksRT.ConnectState.RELIABLE_ONLY.getValue()) {
			ret = reliableConnection.send(csr);
	    
	    	PooledObjects.customRequestPool.push(csr);
	    
	    	return ret;
		}
	       
	  	PooledObjects.customRequestPool.push(csr);
	         
	  	return 0;
	}

	public void log(String tag, GameSparksRT.LogLevel level, String msg) {
		if (GameSparksRT.shouldLog(tag, level)) {
	    	submitAction(LogCommand.pool.pop().configure(this, tag, level, msg));
		}
	}
}
