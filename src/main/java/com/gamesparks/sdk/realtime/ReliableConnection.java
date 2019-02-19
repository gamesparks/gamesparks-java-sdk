package com.gamesparks.sdk.realtime;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

import javax.net.ssl.*;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

class ReliableConnection extends Connection {
	private int remoteport;
	private String remotehost;
	private SocketChannel client;
	private SSLEngine engine = null;
	private ByteBuffer myAppData;
	private ByteBuffer myNetData;
	private ByteBuffer peerAppData;
	private ByteBuffer peerNetData;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private List<byte[]> stringsToSend = new ArrayList<>();
	private List<byte[]> stringsToSendBackup = new ArrayList<>();
	private LinkedList<byte[]> tempPacketsQueue = new LinkedList<byte[]>();
	private Thread1 thread1;
	private Thread2 thread2;
	
	public ReliableConnection(final String remotehost, final int remoteport, final RTSession session) {
		super(session);
		
		this.remotehost = remotehost;
		this.remoteport = remoteport;
		
		thread1 = new Thread1();
		thread2 = new Thread2();
	}
	
	private class Thread1 extends Thread {
		Thread1() {
			super();
			
			start();
	    }
		
		public void run() {
			try {
				Selector selector = Selector.open();
				
				client = SocketChannel.open();
				client.configureBlocking(false);
				client.socket().setTcpNoDelay(true);
				client.socket().setKeepAlive(true);
				client.connect(new InetSocketAddress(remotehost, remoteport));
				
				client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				
				while (client != null && !client.finishConnect()) {
					Thread.sleep(10);
		    	}
				
				if (client == null) {
					//session.log("ReliableConnection", GameSparksRT.LogLevel.DEBUG, " TCP Connection Error");
					
					return;
				}
				
				SSLContext context = SSLContext.getInstance("TLS");
				
				context.init(null, null, new SecureRandom());
				
				engine = context.createSSLEngine(remotehost, remoteport);
				engine.setUseClientMode(true);
				
				SSLSession sslSession = engine.getSession();
				
		        myAppData = ByteBuffer.allocate(GameSparksRT.MAX_UNRELIABLE_MESSAGE_SIZE_BYTES);
		        myNetData = ByteBuffer.allocate(sslSession.getPacketBufferSize());
		        peerAppData = ByteBuffer.allocate(GameSparksRT.MAX_UNRELIABLE_MESSAGE_SIZE_BYTES);
		        peerNetData = ByteBuffer.allocate(sslSession.getPacketBufferSize());
				
				engine.beginHandshake();
				
				if (doHandshake()) {
					if (!stopped && session != null) {
						boolean logged = false;
						
						session.log("ReliableConnection", GameSparksRT.LogLevel.DEBUG, " TCP Connection Established");						  
					
						while (client != null && client.isConnected()) {
							if (!stopped) {
								int readyChannels = selector.select();
								
								if (readyChannels != 0) {
									Set<SelectionKey> selectedKeys = selector.selectedKeys();
									Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

									while (keyIterator.hasNext()) {
										SelectionKey key = keyIterator.next();
										
										if (key.isReadable()) {
											peerNetData.clear();
											
											int bytesRead = client.read(peerNetData);
																
								            if (bytesRead > 0) {	
								            	ByteArrayOutputStream buf = new ByteArrayOutputStream();
								            	 
								                peerNetData.flip();
								                								              
								                while (peerNetData.hasRemaining()) {
								                    peerAppData.clear();
								                    
								                    SSLEngineResult result = engine.unwrap(peerNetData, peerAppData);
								                    									                  									                   
								                    switch (result.getStatus()) {
									                    case OK:								                   
									                        peerAppData.flip();		
									                        
									                        buf.write(peerAppData.array(), 0, peerAppData.limit());									                      							                       									                     							                    	
									                        			     						                  
									                        break;
									                        
									                    case BUFFER_OVERFLOW:
									                    	peerAppData = enlargeApplicationBuffer(peerAppData);
									                        
									                        break;
									                        
									                    case BUFFER_UNDERFLOW:
									                    	peerNetData = handleBufferUnderflow(peerNetData);
									                        
									                        break;
									                    
									                    case CLOSED:
									                    	shutdown();
									                        
									                        return;
									                        
									                    default:
									                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
								                    }
								                }
								                
								                synchronized (tempPacketsQueue) {
													tempPacketsQueue.add(buf.toByteArray());
												}
								            }
										} 
										
										if (key.isWritable()) {
											if (logged) {
												synchronized (stringsToSend) {
													for (byte[] string : stringsToSend) {
														stringsToSendBackup.add(string);
													}
													
													stringsToSend.clear();
												}
												
												for (byte[] string : stringsToSendBackup) {
													sendInternal(string);
												}
													
												stringsToSendBackup.clear();
											} else {
												logged = true;
												
												LoginCommand loginCmd = new LoginCommand(session.connectToken);
												
												send(loginCmd);
											}
										} 
										
										keyIterator.remove();
									}
								}								
							}
						    				
							Thread.sleep(1);
						}
						
						if (session != null) { 
							session.log("ReliableConnection", GameSparksRT.LogLevel.DEBUG, " TCP Connection Closed");
						}
					}
					
					if (!stopped) {
						if (session != null) { 
							session.onReady(false);
						}

						shutdown();
					}
				} else {
					if (session != null) {
						session.log("ReliableConnection", GameSparksRT.LogLevel.DEBUG, " TCP Connection Error");
					}
					
					shutdown();
				}
			} catch (Exception e) {
				if (session != null) {
					session.log("ReliableConnection", GameSparksRT.LogLevel.DEBUG, " TCP Connection Error");
				}

				//System.err.println(e);
				e.printStackTrace();
				
				shutdown();
			}
	    }
	}
	
	private class Thread2 extends Thread {
		private LinkedList<byte[]> packetsQueue;
		
		Thread2() {
			super();
			
			start();
	    }
		
		@SuppressWarnings("unchecked")
		public void run() {
			Stream rss = new Stream();
			
			while (!stopped) {
				try {
					synchronized (tempPacketsQueue) {
						packetsQueue = (LinkedList<byte[]>)tempPacketsQueue.clone();
						tempPacketsQueue.clear();
					}
					
					for (int a = 0; a < packetsQueue.size(); a ++) {
						byte[] buf = packetsQueue.get(a);
						
						rss.setPosition(0);
						
						rss.writeChars(buf, 0, buf.length);
						
						int length = rss.getPosition();
						 
				        rss.setPosition(0);
				        
				        while (rss.getPosition() < length) {
				        	int bytesRead2 = 0;
				        	Packet p = PooledObjects.packetPool.pop();
				        					
				        	if (!stopped) {
				        		bytesRead2 = read(rss, p);
				        	}
				        	
				        	if (session != null) {
				        		onPacketReceived(p, bytesRead2);
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
	
	public int send(RTRequest request) throws Exception {
		if (client != null && client.isConnected()) {
			Stream stream = new Stream();
			Packet p = request.toPacket(session, false);
			
			int ret = Packet.serializeLengthDelimited(stream, p);
			
			synchronized (stringsToSend) {
				stringsToSend.add(stream.getBytes());
			}
			
			PooledObjects.packetPool.push(p);
			    
			return ret;
		} else {
			return -1;
		}
	}
	
	private int read(Stream stream, Packet p) throws Exception {
	  	if (stopped) {
	  		return 0;
	  	}
	  
	  	p.session = session;
	  	p.reliable = true;
	  
	  	int ret = Packet.deserializeLengthDelimited(stream, p);
	 
	  	if (p.reliable == null) {
	  		p.reliable = true;
	  	}
	 
	  	return ret;
	}

	@Override
	protected void stopInternal() {	
		executor.shutdown();
		
		thread1 = null;
		thread2 = null;
		
		try {
			if (engine != null) {
				engine.closeOutbound();
				doHandshake();
				
				//Thread.sleep(2000);
			}
		
			if (client != null && client.isConnected()) {
				client.close();
			}
		} catch (Exception e) {
		}
	
		engine = null;
		client = null;
		session = null;
	}
	
	private boolean doHandshake() throws IOException {
        SSLEngineResult result;
        HandshakeStatus handshakeStatus;
        int appBufferSize = engine.getSession().getApplicationBufferSize();
        ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
        ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);
        
        myNetData.clear();
        peerNetData.clear();

        handshakeStatus = engine.getHandshakeStatus();
        while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (handshakeStatus) {
            	case NEED_UNWRAP:
	                if (client.read(peerNetData) < 0) {
	                    if (engine.isInboundDone() && engine.isOutboundDone()) {
	                        return false;
	                    }
	                    try {
	                        engine.closeInbound();
	                    } catch (SSLException e) {
	                        System.err.println("This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.");
	                    }	           
	                    engine.closeOutbound();	                 
	                    handshakeStatus = engine.getHandshakeStatus();
	                    
	                    break;
	                }	             
	                peerNetData.flip();	              
	                try {
	                    result = engine.unwrap(peerNetData, peerAppData);
	                    peerNetData.compact();
	                    handshakeStatus = result.getHandshakeStatus();
	                } catch (SSLException sslException) {
	                	System.err.println("A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...");	                    
	                	System.err.println(sslException);
	                	engine.closeOutbound();	                    
	                	handshakeStatus = engine.getHandshakeStatus();
	                    
	                    break;
	                }
	                switch (result.getStatus()) {
		                case OK:
		                    break;
		                    
		                case BUFFER_OVERFLOW:	                 
		                    peerAppData = enlargeApplicationBuffer(peerAppData);
		                    
		                    break;
		                    
		                case BUFFER_UNDERFLOW:
		                    peerNetData = handleBufferUnderflow(peerNetData);
		                    
		                    break;
		                    
		                case CLOSED:
		                    if (engine.isOutboundDone()) {
		                        return false;
		                    } else {
		                        engine.closeOutbound();
		                        handshakeStatus = engine.getHandshakeStatus();
		                        
		                        break;
		                    }
		                    
		                default:
		                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
	                }
	                
	                break;
	                
	            case NEED_WRAP:
	                myNetData.clear();
	                try {
	                    result = engine.wrap(myAppData, myNetData);
	                    handshakeStatus = result.getHandshakeStatus();
	                } catch (SSLException sslException) {
	                    System.err.println("A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...");
	                    System.err.println(sslException);
	                    engine.closeOutbound();
	                    handshakeStatus = engine.getHandshakeStatus();
	                    
	                    break;
	                }
	                
	                switch (result.getStatus()) {
		                case OK :
		                    myNetData.flip();
		                    while (myNetData.hasRemaining()) {
		                        client.write(myNetData);
		                    }
		                    
		                    break;
		                case BUFFER_OVERFLOW:		                
		                    myNetData = enlargePacketBuffer(myNetData);
		                    
		                    break;
		                
		                case BUFFER_UNDERFLOW:
		                    throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
		                
		                case CLOSED:
		                    try {
		                        myNetData.flip();
		                        while (myNetData.hasRemaining()) {
		                            client.write(myNetData);
		                        }	                      
		                        peerNetData.clear();
		                    } catch (Exception e) {
		                        System.err.println("Failed to send server's CLOSE message due to socket channel's failure.");
		                        handshakeStatus = engine.getHandshakeStatus();
		                    }
		                    
		                    break;
		                
		                default:
		                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
		            }
	                
	                break;
	            
	            case NEED_TASK:
	                Runnable task;
	                
	                while ((task = engine.getDelegatedTask()) != null) {
	                    executor.execute(task);
	                }
	                handshakeStatus = engine.getHandshakeStatus();
	                
	                break;
	            
	            case FINISHED:
	                break;
	            
	            case NOT_HANDSHAKING:
	                break;
	            
	            default:
	                throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
            }
        }

        return true;
	}
	
	private ByteBuffer enlargePacketBuffer(ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
    }

    private ByteBuffer enlargeApplicationBuffer(ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    private ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        
        return buffer;
    }
    
    private ByteBuffer handleBufferUnderflow(ByteBuffer buffer) {
        if (engine.getSession().getPacketBufferSize() < buffer.limit()) {
            return buffer;
        } else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(buffer);
            
            buffer.flip();
            replaceBuffer.put(buffer);
            
            return replaceBuffer;
        }
    }

    protected void sendInternal(byte[] message) throws IOException {
        myAppData.clear();
        myAppData.put(message);
        myAppData.flip();
        while (myAppData.hasRemaining()) {
            myNetData.clear();
            
            SSLEngineResult result = engine.wrap(myAppData, myNetData);
            
            switch (result.getStatus()) {
	            case OK:
	                myNetData.flip();
	                while (myNetData.hasRemaining()) {
	                    client.write(myNetData);               
	                }
	                	         
	                break;
	            
	            case BUFFER_OVERFLOW:
	                myNetData = enlargePacketBuffer(myNetData);
	            
	                break;
	            case BUFFER_UNDERFLOW:
	                throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
	                
	            case CLOSED:
	                shutdown();
	                
	                return;
	            
	            default:
	                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        }
    }
}
