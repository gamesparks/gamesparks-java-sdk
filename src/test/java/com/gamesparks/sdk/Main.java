package com.gamesparks.sdk;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.gamesparks.sdk.api.GSData;
import com.gamesparks.sdk.api.autogen.GSMessageHandler;
import com.gamesparks.sdk.api.autogen.GSRequestBuilder;
import com.gamesparks.sdk.api.autogen.GSMessageHandler.MatchFoundMessage;
import com.gamesparks.sdk.api.autogen.GSMessageHandler.MatchNotFoundMessage;
import com.gamesparks.sdk.api.autogen.GSMessageHandler.ScriptMessage;
import com.gamesparks.sdk.api.autogen.GSRequestBuilder.AuthenticationRequest;
import com.gamesparks.sdk.api.autogen.GSRequestBuilder.LogEventRequest;
import com.gamesparks.sdk.api.autogen.GSRequestBuilder.MatchmakingRequest;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder.AroundMeLeaderboardResponse;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder.AuthenticationResponse;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder.LogEventResponse;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder.MatchmakingResponse;
import com.gamesparks.sdk.api.autogen.GSTypes.LeaderboardData;
import com.gamesparks.sdk.realtime.GameSparksRT;
import com.gamesparks.sdk.realtime.RTData;
import com.gamesparks.sdk.tests.*;

public class Main {
	
	private static int numCycles = 0;
	private static GS mGSRTInstance;
	private static GSExtended mCurrInstance;
	private static GameSession mGameSession;
	
	public static void part2() {
		LogEventRequest request = mCurrInstance.getRequestBuilder().createLogEventRequest()
				.setEventKey("001")
				.setEventAttribute("0001", "AAAH");

		request.setDurable(true);
		request.send(new GSEventConsumer<LogEventResponse>() {
			@Override
			public void onEvent(LogEventResponse logEventResponse) {
				try {
					GSData scriptData = logEventResponse.getScriptData();
					System.out.println("numberList: " + scriptData.getNumberList("numberList"));
					System.out.println("booleanList: " + scriptData.getBooleanList("booleanList"));
					System.out.println("stringList: " + scriptData.getStringList("stringList"));
				} catch (Exception e) {
					System.out.println(e.toString());
				}
			}
		});
	}
	
	public static void part1() {
		try {
			GSHelperMethods.waitForDeviceAuthentication(mCurrInstance, new GSHelperMethods.Callback() {
				@Override
				public void function() {
					try {
						part2();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendMatchmakingRequest() {
		MatchmakingRequest request = mGSRTInstance.getRequestBuilder().createMatchmakingRequest();
		
		request.setSkill(1);
		request.setMatchShortCode("Match_STD");
		request.send(new GSEventConsumer<MatchmakingResponse>() {
			@Override
			public void onEvent(MatchmakingResponse event) {
				if (event.hasErrors()) {
					System.err.println(event.getAttribute("error"));
				} else {
					System.out.println("Match OK...");
				}
			}			
		});
	}
	
	public static void main(String[] args) {
		/*try {
			mCurrInstance = GSHelperMethods.createInstanceWaitForAvailable("test", new GSHelperMethods.Callback() {
				@Override
				public void function() {
					try {
						part1();				
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		mGSRTInstance = new GS("exampleKey12", "exampleSecret1234567890123456789", "", false, true, new TestPlatform());
		
		mGSRTInstance.setOnAvailable(new GSEventConsumer<Boolean>() {
			@Override
			public void onEvent(Boolean available) {
				System.out.println("Availability: " + available);
				
				if (available) {
					mGSRTInstance.getRequestBuilder().createDeviceAuthenticationRequest()
						.setDeviceId((String.valueOf((new Random()).nextInt(1000))))
						.setDeviceOS("MacOS")
						.send(new GSEventConsumer<AuthenticationResponse>() {
							@Override
							public void onEvent(AuthenticationResponse event) {
								if (event.hasErrors()) {
									System.err.println(event.getAttribute("error"));
								} else {
									sendMatchmakingRequest();
								}
							}		
						});
				}
			}
		});
		
		mGSRTInstance.getMessageHandler().setMatchFoundMessageListener(new GSEventConsumer<GSMessageHandler.MatchFoundMessage>() {
			@Override
			public void onEvent(MatchFoundMessage event) {
				mGameSession = new GameSession(event.getAccessToken(), event.getHost(), event.getPort());
			}
		});
		
		mGSRTInstance.getMessageHandler().setMatchNotFoundMessageListener(new GSEventConsumer<GSMessageHandler.MatchNotFoundMessage>() {
			@Override
			public void onEvent(MatchNotFoundMessage event) {
				System.err.println("MATCH NOT FOUND");
				
				sendMatchmakingRequest();
			}
		});
		
		mGSRTInstance.start();
		
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					if (mGameSession != null) {
						mGameSession.mSession.update();
						
						if (mGameSession.ready) {
							RTData data = new RTData();
							
							data.setLong(1, numCycles);
							
							try {								
								mGameSession.mSession.sendRTData(1, GameSparksRT.DeliveryIntent.UNRELIABLE, data, new int[]{});
							} catch (Exception e) {
								e.printStackTrace();
								
							}
							
							numCycles ++;
						}
					}
					
					try {
						Thread.sleep(1000 / 60);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		
		thread.start();
	}
}
