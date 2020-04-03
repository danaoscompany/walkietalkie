package com.prod.walkietalkie;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import io.agora.openlive.voice.only.model.ConstantApp;
import io.agora.openlive.voice.only.ui.BaseActivity;
import io.agora.openlive.voice.only.ui.LiveRoomActivity;
import io.agora.rtc.Constants;

public class MainActivity extends BaseActivity
{
	@Override
	protected void initUIandEvent()
	{
		// TODO: Implement this method
		/*startActivity(new Intent(this, MainActivity.class));
		finish();*/
		//write("current_channel", "channel_00_01");
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

				@Override
				public void run() {
					Intent i = new Intent(MainActivity.this, LiveRoomActivity.class);
					i.putExtra(ConstantApp.ACTION_KEY_CROLE, Constants.CLIENT_ROLE_AUDIENCE);
					i.putExtra(ConstantApp.ACTION_KEY_ROOM_NAME, "channel_00_01");
					startActivity(i);
					MainActivity.this.finish();
				}
		}, 1000);
		//finish();
	}

	@Override
	protected void deInitUIandEvent()
	{
		// TODO: Implement this method
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
}
