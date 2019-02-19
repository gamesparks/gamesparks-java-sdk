package com.gamesparks.sdk.realtime;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;

class FastConnection extends Connection {
	private int reconnectSleep;
	private int remoteport;
	private String remotehost;
	private InetSocketAddress address;
	private DatagramChannel client;
	private LinkedList<ByteBuffer> tempPacketsQueue = new LinkedList<ByteBuffer>();
	private Thread1 thread1;
	private Thread2 thread2;

	public FastConnection(RTSession session) {
		super(session);
		
		reconnectSleep = 100;
				  
		remotehost = "";
		remoteport = 0;
	}
	
	private class Thread1 extends Thread {
		private LinkedList<ByteBuffer> packetsQueue;
		
		Thread1() {
			super();
			
			start();
	    }
		
		@SuppressWarnings("unchecked")
		public void run() {
			Stream ms = new Stream();
			
			while (!stopped) {
				try {
					synchronized (tempPacketsQueue) {
						packetsQueue = (LinkedList<ByteBuffer>)tempPacketsQueue.clone();
						tempPacketsQueue.clear();
					}
					
					for (int a = 0; a < packetsQueue.size(); a ++) {
						ByteBuffer buf = packetsQueue.get(a);
						
						ms.setPosition(0);
							
						ms.writeChars(buf.array(), 0, buf.limit());
						
						int read = ms.getPosition();
						
						ms.setPosition(0);
							
					    while (ms.getPosition() < read) {								
					    	int packetSize = 0;
					    	Packet p = PooledObjects.packetPool.pop();						
					    
					    	if (!stopped) {
					    		p.session = session;
					    		p.reliable = false;
					        
					    		packetSize = Packet.deserializeLengthDelimited(ms, p);
					    		
					    		if (p.reliable == null) {
					    			p.reliable = false;
					    		}
					    	}
					      				
					    	if (session != null) {
					    		onPacketReceived(p, packetSize);				
					    	}
					    	
					    	PooledObjects.packetPool.push(p);
					    }
					}
					
					packetsQueue.clear();
					
					Thread.sleep(1);
				} catch (Exception e) {	
					e.printStackTrace();
				}
			}
		}
	}
	
	private class Thread2 extends Thread {
		Thread2() {
			super();
			
			start();
	    }
		
		public void run() {
			try {
				address = new InetSocketAddress(remotehost, remoteport);
				
				client = DatagramChannel.open();
				client.configureBlocking(false);
				client.connect(address);
				
				if (client.isConnected()) {
					if (session != null) {
						session.log("FastConnection", GameSparksRT.LogLevel.DEBUG, " UDP Socket Connected");
					      
					    session.log("FastConnection", GameSparksRT.LogLevel.DEBUG, " local=" + InetAddress.getLocalHost() + " remote=" + remotehost + ":" + remoteport);
					  
					    doLogin();
					    
					    ByteBuffer buf = ByteBuffer.allocate(GameSparksRT.MAX_UNRELIABLE_MESSAGE_SIZE_BYTES);
					      
						while (!stopped && client != null && client.isConnected()) {
							int ret = client.read(buf);
							
							if (ret > 0) {
								ByteBuffer clone = ByteBuffer.allocate(buf.capacity());
							    
								buf.flip();
							    clone.put(buf);
							    clone.flip();
							    
								synchronized (tempPacketsQueue) {
									tempPacketsQueue.add(clone);
								}
							}
							
							buf.clear();
							
							Thread.sleep(1);
						}
					} else {
						shutdown();
					}
				}
				
				if (session != null) {
					session.log("FastConnection", GameSparksRT.LogLevel.DEBUG, " UDP Socket Disconnected");
				}
			} catch (Exception e) {
				if (session != null) {
					session.log("FastConnection", GameSparksRT.LogLevel.DEBUG, " UDP Connection Error");
				}

				//System.err.println(e);
				e.printStackTrace();
				
				shutdown();
			}
		}
	}
	
	public void start(final String remotehost, final int remoteport) {
		this.remotehost = remotehost;
		this.remoteport = remoteport;
		
		thread1 = new Thread1();
		thread2 = new Thread2();
	}
	
	private void doLogin() throws Exception {
		if (session != null) {
			if (session.connectState.getValue() < GameSparksRT.ConnectState.RELIABLE_AND_FAST_SEND.getValue()) {
				LoginCommand loginCmd = new LoginCommand(session.connectToken);
	  
				send(loginCmd);
				
				new java.util.Timer().schedule(new java.util.TimerTask() {
		            @Override
		            public void run() {
		                try {
							doLogin();
						} catch (Exception e) {
							System.err.println(e);
						}
		            }
				}, reconnectSleep);
			} else {
				session.onReady(true);
			}
		}
	}
	
	public int send(RTRequest request) throws Exception {
		if (client != null && client.isConnected()) {
			int ret;
		    Stream ms = PooledObjects.memoryStreamPool.pop();
		  
		    ms.setPosition(0);
		    
		    Packet p = request.toPacket(session, true);
		    
			Packet.serializeLengthDelimited(ms, p);
		    
		    PooledObjects.packetPool.push(p);
		    
		    ByteBuffer buf = ByteBuffer.allocate(ms.getPosition());
		    
		    buf.put(ms.getBytes(ms.getPosition()));
		    buf.rewind();
		    
		    client.write(buf);
		    
		    ret = ms.getPosition();
		    
		    PooledObjects.memoryStreamPool.push(ms);
			    
			return ret;
		} else {
			return -1;
		}
	}

	@Override
	protected void stopInternal() {
		thread1 = null;
		thread2 = null;
		
		if (client != null && client.isConnected()) {
			try {
				client.close();
			} catch (Exception e) {
				
			}
		}

		client = null;	  
		session = null;
	}
}
