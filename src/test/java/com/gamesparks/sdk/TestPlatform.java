package com.gamesparks.sdk;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.jar.Attributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.gamesparks.sdk.api.GSData;

public class TestPlatform implements IGSPlatform {
	
	private String playerId;
	private String authToken;
	
	public TestPlatform() {
		GSData data = getDeviceStats();
		
		logMessage(Arrays.asList(data.getBaseData()).toString()); 
	}
	
	@Override
	public File getWritableLocation() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public void executeOnMainThread(Runnable job) {
		job.run();
	}

	@Override
	public String getPlayerId() {
		return playerId;
	}

	@Override
	public String getAuthToken() {
		return authToken;
	}

	@Override
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	@Override
	public void setAuthToken(String authToken) {
		logMessage("Got authtoken " + authToken);
		
		this.authToken = authToken;
	}

	@Override
	public String getHmac(String data, String secret) {
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
			
			sha256_HMAC.init(secret_key);
			
			return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
		} catch(Exception e) {
			return null;
		}

	}

	@Override
	public void logMessage(String msg) {
		System.out.println(msg);
		
	}

	@Override
	public void logError(Throwable t) {
		t.printStackTrace();
	}

	@Override
	public String getDeviceId() {
		String uuid = "";
		
		try {
			File location = new File(getWritableLocation().getAbsolutePath(), "gamesparks.deviceid");
			
			if (location != null && location.exists()) {
				FileReader fr = new FileReader(location.getAbsoluteFile());
				BufferedReader br = new BufferedReader(fr);
				
				uuid = br.readLine();
				
				br.close();
			}
			
			if (uuid.length() <= 0) {
				uuid = UUID.randomUUID().toString();
				
				location = new File(getWritableLocation().getAbsolutePath(), "gamesparks.deviceid");
				
				if (location != null) {
					if (!location.exists()) {
						location.createNewFile();
					}
		
					FileWriter fw = new FileWriter(location.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					
					bw.write(uuid);
					bw.close();
				}
			}
		} catch (Exception e) {
			logError(e);
		}
		
		return uuid;
	}

	@Override
	public String getDeviceOS() {
		String OS = System.getProperty("os.name").toLowerCase();
		
		if (OS.indexOf("win") >= 0) {
			return "WINDOWS";
		} else if (OS.indexOf("mac") >= 0) {
			return "MACOS";
		} else if (OS.indexOf("nux") >= 0) {
			return "LINUX";
		} else {
			return "UNKNOWN";
		}
	}

	@Override
	public String getPlatform() {
		if (getDeviceOS().equals("WINDOWS")) {
			return "Windows";
		} else if (getDeviceOS().equals("MACOS")) {
			return "OSX";
		} else if (getDeviceOS().equals("LINUX")) {
			return "Linux";
		} else {
			return "Unknown";
		}
	}

	@Override
	public String getSDK() {
		return "Java";
	}

	@Override
	public String getDeviceType() {
		return "Desktop";
	}

	@Override
	public GSData getDeviceStats() {
		Map<String, Object> data = new HashMap<String, Object> ();
		String memory = Long.toString(Runtime.getRuntime().maxMemory() / 1024 / 1024);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int)screenSize.getWidth();
		int height = (int)screenSize.getHeight();
		String resolution = Integer.toString(width).concat("x").concat(Integer.toString(height));
		
		if (getDeviceOS().equals("MACOS")) {
			data.put ("manufacturer", "Apple");
		} else {
			data.put ("manufacturer", "Unknown");
		}
		data.put ("model", "Unknown");
		data.put ("memory", memory.concat("MB"));
		data.put ("os.name", System.getProperty("os.name"));
		data.put ("os.version", System.getProperty("os.version"));
		data.put ("cpu.cores",Integer.toString(Runtime.getRuntime().availableProcessors()));
		data.put ("cpu.vendor", System.getProperty("os.arch"));
		data.put ("resolution", resolution);
		data.put ("gssdk", "0.9.0");
		data.put ("engine", getSDK());
		data.put ("engine.version", System.getProperty("java.version"));

		return new GSData (data);
	}

}
