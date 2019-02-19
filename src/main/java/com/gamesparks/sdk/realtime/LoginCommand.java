package com.gamesparks.sdk.realtime;

class LoginCommand extends RTRequest {
	private static final int CLIENT_VERSION = 2;
	
	private String token;
	
	public LoginCommand(String token) {
		super();
		
		opCode = 0;
		this.token = token;
	}
	
	public void serialize(Stream stream) throws Exception {
		if (token != null) {
			stream.writeByte(10);
			ProtocolParser.writeString(stream, token);
		}
		
		stream.writeByte(16);
		ProtocolParser.writeUInt64(stream, CLIENT_VERSION);
	}
}
