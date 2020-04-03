package com.prod.walkietalkie;

import io.agora.openlive.voice.only.ui.BaseActivity;
import android.os.Bundle;
import android.view.View;
import io.agora.openlive.voice.only.model.ConstantApp;
import io.agora.rtc.Constants;
import io.agora.rtc.models.ChannelMediaOptions;
import io.agora.rtc.RtcChannel;

public class TestActivity extends BaseActivity
{
	int pushCount = 0;
	int a = 0;
	int b = 0;
	boolean searchingSignal = false;
	String channel1 = "channel_00_01";
	String channel2 = "channel_00_02";

	@Override
	protected void initUIandEvent()
	{
		// TODO: Implement this method
	}

	@Override
	protected void deInitUIandEvent()
	{
		// TODO: Implement this method
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
	}
	
	public void a(View view) {
		searchingSignal = true;
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					write("token", token);
					for (int i=0; i<pushCount; i++) {
						rtcEngine().leaveChannel();
					}
					pushCount = 0;
					ChannelMediaOptions opt = new ChannelMediaOptions();
					opt.autoSubscribeAudio = true;
					opt.autoSubscribeVideo = false;
					worker().getRtcEngine().createRtcChannel(channel1).joinChannel(token, null, 0, opt);
					pushCount++;
					get(new Listener() {

							@Override
							public void onResponse(String token) {
								write("token", token);
								/*ChannelMediaOptions opt = new ChannelMediaOptions();
								opt.autoSubscribeAudio = true;
								opt.autoSubscribeVideo = false;
								RtcChannel c = worker().getRtcEngine().createRtcChannel(channel2);
								c.joinChannel(token, null, 0, opt);
								c.publish();*/
								worker().joinChannel(channel2, token, 0);
								pushCount++;
								searchingSignal = false;
							}
						}, ConstantApp.TOKEN_GENERATION_URL+channel2);
				}
			}, ConstantApp.TOKEN_GENERATION_URL+channel1);
	}
	
	public void b(View view) {
		searchingSignal = true;
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					write("token", token);
					for (int i=0; i<pushCount; i++) {
						rtcEngine().leaveChannel();
					}
					pushCount = 0;
					worker().joinChannel(channel1, token, config().mUid);
					pushCount++;
					searchingSignal = false;
				}
			}, ConstantApp.TOKEN_GENERATION_URL+channel1);
	}
	
	public void c(View view) {
		searchingSignal = true;
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					write("token", token);
					for (int i=0; i<pushCount; i++) {
						rtcEngine().leaveChannel();
					}
					pushCount = 0;
					worker().joinChannel(channel2, token, config().mUid);
					pushCount++;
					searchingSignal = false;
				}
			}, ConstantApp.TOKEN_GENERATION_URL+channel2);
	}
	
	public void speak(View view) {
		worker().configEngine(Constants.CLIENT_ROLE_BROADCASTER);
	}
	
	public void stopSpeaking(View view) {
		worker().configEngine(Constants.CLIENT_ROLE_AUDIENCE);
	}
}
