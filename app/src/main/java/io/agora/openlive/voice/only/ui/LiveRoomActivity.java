package io.agora.openlive.voice.only.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prod.walkietalkie.PushItem;
import com.prod.walkietalkie.R;
import io.agora.openlive.voice.only.model.AGEventHandler;
import io.agora.openlive.voice.only.model.ConstantApp;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.IRtcEngineEventHandler.AudioVolumeInfo;
import io.agora.rtc.IRtcEngineEventHandler.RtcStats;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.models.ChannelMediaOptions;
import java.util.ArrayList;
import io.agora.rtc.RtcChannel;

public class LiveRoomActivity extends BaseActivity implements AGEventHandler
{

	LinearLayout broadcaster;
	LinearLayout lcd;
    boolean lcdActive = false;
    private volatile boolean mAudioMuted = false;
    private volatile int mAudioRouting = -1; // Default
	TextView connectionStatusView, connectionCountView, channelView;
	boolean searchingSignal = false;
	RelativeLayout noSignal;
	boolean isPublic = false;
	Context mContext;
	boolean firstTime = true;
	//RtcChannel internationalChannel = null;
	int pushCount = 0;
	DatabaseReference ref = null;
	ValueEventListener l = null;
	ArrayList<PushItem> items = new ArrayList<>();
	ArrayList<RtcChannel> channels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);
		broadcaster = findViewById(R.id.broadcaster);
		lcd = findViewById(R.id.lcd);
		connectionStatusView = findViewById(R.id.connection_status);
		connectionCountView = findViewById(R.id.connection_count);
		noSignal = findViewById(R.id.no_signal);
		channelView = findViewById(R.id.channel);
		Typeface digital = Typeface.createFromAsset(getAssets(), "Digital.ttf");
		connectionStatusView.setTypeface(digital);
		connectionCountView.setTypeface(digital);
		channelView.setTypeface(digital);
		/*FirebaseDatabase.getInstance().getReference("users").child("current_active_device").addValueEventListener(new ValueEventListener() {

		 @Override
		 public void onDataChange(DataSnapshot dataSnapshot) {
		 String myDeviceID = getDeviceID();
		 String currentActiveDevice = dataSnapshot.getValue(String.class);
		 if (currentActiveDevice != null && currentActiveDevice.equals("") && !currentActiveDevice.equals(myDeviceID)) {
		 if (firstTime) {
		 firstTime = false;
		 } else {
		 MediaPlayer mp = MediaPlayer.create(LiveRoomActivity.this, R.raw.toneend);
		 mp.start();
		 }
		 } else if (currentActiveDevice != null && !currentActiveDevice.equals("") && !currentActiveDevice.equals(myDeviceID)) {
		 MediaPlayer mp = MediaPlayer.create(LiveRoomActivity.this, R.raw.tonestart);
		 mp.start();
		 }
		 }

		 @Override
		 public void onCancelled(DatabaseError p1) {
		 }
		 });*/
		broadcaster.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View p1, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (!lcdActive) {
							return false;
						}
						MediaPlayer mp = MediaPlayer.create(LiveRoomActivity.this, R.raw.tonestart);
						mp.start();
						doConfigEngine(Constants.CLIENT_ROLE_BROADCASTER);
						/*FirebaseDatabase.getInstance().getReference("users").child("current_active_device").addListenerForSingleValueEvent(new ValueEventListener() {

								@Override
								public void onDataChange(DataSnapshot dataSnapshot)
								{
									// TODO: Implement this method
									String activeDeviceID = dataSnapshot.getValue(String.class);
									if (activeDeviceID == null) {
										activeDeviceID = "";
									}
									activeDeviceID = activeDeviceID.trim();
									if (!activeDeviceID.equals("")) {
										FirebaseDatabase.getInstance().getReference("users").child("current_active_device").addListenerForSingleValueEvent(new ValueEventListener() {

												@Override
												public void onDataChange(DataSnapshot dataSnapshot)
												{
													// TODO: Implement this method
													String currentActiveDeviceID = dataSnapshot.getValue(String.class);
													if (currentActiveDeviceID == null || currentActiveDeviceID.trim().equals("")) {
														return;
													}
													FirebaseDatabase.getInstance().getReference("users").child(currentActiveDeviceID).child("active").setValue(0);
													FirebaseDatabase.getInstance().getReference("users").child(getDeviceID()).child("active").setValue(1);
													FirebaseDatabase.getInstance().getReference("users").child("current_active_device").setValue(getDeviceID());
												}

												@Override
												public void onCancelled(DatabaseError p1)
												{
													// TODO: Implement this method
												}
											});
										doConfigEngine(Constants.CLIENT_ROLE_BROADCASTER);
									} else {
										FirebaseDatabase.getInstance().getReference("users").child(getDeviceID()).child("active").setValue(1);
										FirebaseDatabase.getInstance().getReference("users").child("current_active_device").setValue(getDeviceID());
									}
								}

								@Override
								public void onCancelled(DatabaseError p1)
								{
									// TODO: Implement this method
								}
							});*/
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						//FirebaseDatabase.getInstance().getReference("users").child("current_active_device").removeValue();
						MediaPlayer mp = MediaPlayer.create(LiveRoomActivity.this, R.raw.toneend);
						mp.start();
						doConfigEngine(Constants.CLIENT_ROLE_AUDIENCE);
					}
					return false;
				}
			});
    }
	
	public String getChannelName() {
		if (isPublic) {
			return "channel_00_00";
		} else {
			return read("current_channel", "channel_00_01");
		}
	}

	public void editChannel(View view0) {
		if (isPublic) {
			return;
		}
		View view = LayoutInflater.from(this).inflate(R.layout.edit_channel, null);
		final EditText channelField = view.findViewById(R.id.channel);
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setView(view)
			.setTitle("Edit Channel")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface p1, int p2) {
					String tmpChannel = channelField.getText().toString().trim();
					if (tmpChannel.equals("")) {
						show("Mohon masukkan channel");
						return;
					}
					if (tmpChannel.length() != 4) {
						show("Mohon masukkan nama channel sepanjang 4 karakter");
						return;
					}
					tmpChannel = tmpChannel.substring(0, 2)+"_"+tmpChannel.substring(2, 4);
					final String channel = tmpChannel;
					String prevChannel = read("current_channel", "channel_00_01");
					write("current_channel", "channel_"+channel);
					String currentChannel = read("current_channel", "channel_00_01");
					updateChannelCount(prevChannel, currentChannel);
					String token = getToken("channel_00_00");
					write("token", token);
					currentChannel = read("current_channel", "channel_00_01");
					for (int i=0; i<pushCount; i++) {
						pop();
					}
					pushCount = 0;
					ChannelMediaOptions opt = new ChannelMediaOptions();
					opt.autoSubscribeAudio = true;
					opt.autoSubscribeVideo = false;
					RtcChannel c = worker().getRtcEngine().createRtcChannel("channel_00_00");
					c.joinChannel(token, null, 0, opt);
					pushCount++;
					push(c);
					token = getToken(currentChannel);
					worker().joinChannel(currentChannel, token, 0);
					pushCount++;
					push();
					channelView.setText(currentChannel.replace("channel_", "").replace("_", ".").trim());
					searchingSignal = false;
					noSignal.setVisibility(View.GONE);
				}
			})
			.setNegativeButton("Batal", null)
			.create();
		dialog.show();
	}
	
	public void push(RtcChannel channel) {
		PushItem item = new PushItem();
		item.type = PushItem.TYPE_CHANNEL;
		item.channel = channel;
		items.add(item);
	}
	
	public void push() {
		PushItem item = new PushItem();
		item.type = PushItem.TYPE_ENGINE;
		item.channel = null;
		items.add(item);
	}
	
	public void pop() {
		if (items.size() > 0) {
			PushItem item = items.get(items.size()-1);
			if (item != null) {
				if (item.type == PushItem.TYPE_CHANNEL) {
					item.channel.leaveChannel();
				} else if (item.type == PushItem.TYPE_ENGINE) {
					worker().getRtcEngine().leaveChannel();
				}
				items.remove(items.size()-1);
			}
		}
	}
	
	public void clearAll() {
		for (int i=0; i<10; i++) {
			try {
				worker().getRtcEngine().leaveChannel();
			} catch (Exception e) {}
		}
		for (int i=0; i<channels.size(); i++) {
			channels.get(i).leaveChannel();
		}
		channels.clear();
	}
	
	public void changeToGroup(View view) {
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		String prevChannel = getChannelName();
		isPublic = true;
		write("is_public", 1);
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		updateChannelCount(prevChannel, "international");
		String token = getToken("channel_00_00");
		write("token", token);
		for (int i=0; i<pushCount; i++) {
			pop();
		}
		pushCount = 0;
		worker().getRtcEngine().joinChannel("channel_00_00", token, null, 0);
		push();
		pushCount++;
		channelView.setText("channel_00_00");
		searchingSignal = false;
		noSignal.setVisibility(View.GONE);
		searchingSignal = false;
	}
	
	/*public void changeToGroup(View view) {
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		String prevChannel = getChannelName();
		isPublic = true;
		write("is_public", 1);
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		updateChannelCount(prevChannel, "international");
		//show("Joining channel 2...");
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					write("token", token);
					for (int i=0; i<pushCount; i++) {
						rtcEngine().leaveChannel();
					}
					pushCount = 0;
					worker().joinChannel("international", token, config().mUid);
					pushCount++;
					final String token0 = token;
					get(new Listener() {

							@Override
							public void onResponse(String token) {
								worker().joinChannel("channel_00_00", token, config().mUid);
								pushCount++;
								channelView.setText("channel_00_00");
								searchingSignal = false;
								noSignal.setVisibility(View.GONE);
							}
						}, ConstantApp.TOKEN_GENERATION_URL+"channel_00_00");
				}
			}, ConstantApp.TOKEN_GENERATION_URL+"international");
	}*/
	
	/*public void changeToGroup(View view) {
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		String prevChannel = getChannelName();
		isPublic = true;
		write("is_public", 1);
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		final String currentChannel = "channel_00_00";
		updateChannelCount(prevChannel, currentChannel);
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					write("token", token);
					for (int i=0; i<pushCount; i++) {
						rtcEngine().leaveChannel();
					}
					pushCount = 0;
					worker().joinChannel(currentChannel, config().mUid);
					pushCount++;
					channelView.setText("channel_00_00");
					searchingSignal = false;
					noSignal.setVisibility(View.GONE);
				}
			}, ConstantApp.TOKEN_GENERATION_URL+currentChannel);
	}*/
	
	/*public void changeToGroup(View view) {
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		isPublic = true;
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		//write("current_channel", "channel_00_00");
		final String currentChannel = "channel_00_00";
		String prevChannel = getChannelName();
		write("current_channel", currentChannel);
		FirebaseDatabase.getInstance().getReference("user_count").child(prevChannel).child(getDeviceID()).removeValue();
		FirebaseDatabase.getInstance().getReference("user_count").child(getChannelName()).child(getDeviceID()).setValue(1);
		//show("Joining channel 2...");
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					write("token", token);
					for (int i=0; i<pushCount; i++) {
						rtcEngine().leaveChannel();
					}
					pushCount = 0;
					worker().joinChannel(currentChannel, token, config().mUid);
					pushCount++;
					channelView.setText("channel_00_00");
					searchingSignal = false;
					noSignal.setVisibility(View.GONE);
				}
			}, ConstantApp.TOKEN_GENERATION_URL+currentChannel);
	}*/

	/*public void changeToGroup(View view) {
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		//show("Joining channel 2...");
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					write("token", token);
					for (int i=0; i<pushCount; i++) {
						rtcEngine().leaveChannel();
					}
					pushCount = 0;
					worker().joinChannel("international", token, config().mUid);
					pushCount++;
					channelView.setText("channel_00_00");
					searchingSignal = false;
					noSignal.setVisibility(View.GONE);
				}
			}, ConstantApp.TOKEN_GENERATION_URL+"international");
	}*/
	
	public void changeToPrivate(View view) {
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		String prevChannel = getChannelName();
		isPublic = false;
		write("is_public", 0);
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		String currentChannel = read("current_channel", "channel_00_01");
		write("current_channel", currentChannel);
		updateChannelCount(prevChannel, getChannelName());
		String token = getToken("channel_00_00");
		write("token", token);
		for (int i=0; i<pushCount; i++) {
			pop();
		}
		pushCount = 0;
		currentChannel = read("current_channel", "channel_00_01");
		ChannelMediaOptions opt = new ChannelMediaOptions();
		opt.autoSubscribeAudio = true;
		opt.autoSubscribeVideo = false;
		RtcChannel c = worker().getRtcEngine().createRtcChannel("channel_00_00");
		c.joinChannel(token, null, 0, opt);
		push(c);
		pushCount++;
		token = getToken(currentChannel);
		write("token", token);
		/*ChannelMediaOptions opt = new ChannelMediaOptions();
		 opt.autoSubscribeAudio = true;
		 opt.autoSubscribeVideo = false;
		 RtcChannel c = worker().getRtcEngine().createRtcChannel(channel2);
		 c.joinChannel(token, null, 0, opt);
		 c.publish();*/
		worker().joinChannel(currentChannel, token, 0);
		push();
		pushCount++;
		channelView.setText(currentChannel.replace("channel_", "").replace("_", ".").trim());
		searchingSignal = false;
		noSignal.setVisibility(View.GONE);
		searchingSignal = false;
	}

	/*public void changeToPrivate(View view) {
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		String prevChannel = getChannelName();
		isPublic = false;
		write("is_public", 0);
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		final String currentChannel = read("current_channel", "channel_00_01");
		write("current_channel", currentChannel);
		updateChannelCount(prevChannel, getChannelName());
		//show("Joining channel 2...");
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					write("token", token);
					final String currentChannel = read("current_channel", "channel_00_01");
					for (int i=0; i<pushCount; i++) {
						rtcEngine().leaveChannel();
					}
					pushCount = 0;
					worker().joinChannel(currentChannel, token, config().mUid);
					pushCount++;
					final String token0 = token;
					get(new Listener() {

							@Override
							public void onResponse(String token) {
								worker().joinChannel("channel_00_00", token, config().mUid);
								pushCount++;
								channelView.setText(currentChannel.replace("channel_", "").replace("_", ".").trim());
								searchingSignal = false;
								noSignal.setVisibility(View.GONE);
							}
						}, ConstantApp.TOKEN_GENERATION_URL+"channel_00_00");
				}
			}, ConstantApp.TOKEN_GENERATION_URL+currentChannel);
	}*/
	
	public void channelUp(View view) {
		if (isPublic) {
			return;
		}
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		String currentChannel = read("current_channel", "channel_00_01");
		String a = currentChannel.substring(8, 10);
		String b = currentChannel.substring(11, 13);
		int aa = Integer.parseInt(a);
		int bb = Integer.parseInt(b);
		if (bb < 99) {
			bb++;
		} else {
			if (aa < 99) {
				bb = 0;
				aa += 1;
			}
		}
		currentChannel = "channel_"+String.format("%02d", aa)+"_"+String.format("%02d", bb);
		write("current_channel", currentChannel);
		show(currentChannel);
		String token = getToken(currentChannel);
		write("token", token);
		for (int i=0; i<pushCount; i++) {
			pop();
		}
		pushCount = 0;
		currentChannel = read("current_channel", "channel_00_01");
		worker().joinChannel(currentChannel, token, config().mUid);
		push();
		pushCount++;
		channelView.setText(currentChannel.replace("channel_", "").replace("_", ".").trim());
		searchingSignal = false;
		noSignal.setVisibility(View.GONE);
		searchingSignal = false;
	}

	/*public void channelUp(View view) {
		if (isPublic) {
			return;
		}
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		//show("Joining channel 2...");
		String currentChannel = read("current_channel", "channel_00_01");
		String a = currentChannel.substring(8, 10);
		String b = currentChannel.substring(11, 13);
		int aa = Integer.parseInt(a);
		int bb = Integer.parseInt(b);
		if (bb < 99) {
			bb++;
		} else {
			if (aa < 99) {
				bb = 0;
				aa += 1;
			}
		}
		currentChannel = "channel_"+String.format("%02d", aa)+"_"+String.format("%02d", bb);
		String prevChannel = getChannelName();
		write("current_channel", currentChannel);
		updateChannelCount(prevChannel, getChannelName());
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					write("token", token);
					final String currentChannel = read("current_channel", "channel_00_01");
					for (int i=0; i<pushCount; i++) {
						//rtcEngine().leaveChannel();
						pop();
					}
					clearAll();
					pushCount = 0;
					ChannelMediaOptions opt = new ChannelMediaOptions();
					opt.autoSubscribeAudio = true;
					opt.autoSubscribeVideo = false;
					RtcChannel c = worker().getRtcEngine().createRtcChannel("channel_00_00");
					c.joinChannel(token, null, 0, opt);
					pushCount++;
					push(c);
					get(new Listener() {

							@Override
							public void onResponse(String token) {
								worker().joinChannel(currentChannel, token, 0);
								pushCount++;
								push();
								channelView.setText(currentChannel.replace("channel_", "").replace("_", ".").trim());
								searchingSignal = false;
								noSignal.setVisibility(View.GONE);
							}
						}, ConstantApp.TOKEN_GENERATION_URL+currentChannel);
				}
			}, ConstantApp.TOKEN_GENERATION_URL+"channel_00_00");
	}*/

	public void updateChannelCount(String prevChannel, final String channelName) {
		if (prevChannel == null) {
			FirebaseDatabase.getInstance().getReference("user_count").child(channelName).child(getDeviceID()).setValue(1).addOnCompleteListener(new OnCompleteListener() {

					@Override
					public void onComplete(Task task) {
						updateChannelCount(channelName);
					}
				});
		} else {
			FirebaseDatabase.getInstance().getReference("user_count").child(prevChannel).child(getDeviceID()).removeValue().addOnCompleteListener(new OnCompleteListener() {

					@Override
					public void onComplete(Task task) {
						FirebaseDatabase.getInstance().getReference("user_count").child(channelName).child(getDeviceID()).setValue(1).addOnCompleteListener(new OnCompleteListener() {

								@Override
								public void onComplete(Task task) {
									updateChannelCount(channelName);
								}
							});
					}
				});
		}
	}
	
	public void updateChannelCount(String channelName) {
		if (ref != null && l != null) {
			ref.removeEventListener(l);
		}
		ref = FirebaseDatabase.getInstance().getReference("user_count").child(channelName);
		ref.addValueEventListener(new ValueEventListener() {

				@Override
				public void onDataChange(DataSnapshot dataSnapshot)
				{
					l = this;
					long userCount = dataSnapshot.getChildrenCount();
					connectionCountView.setText(""+userCount);
				}

				@Override
				public void onCancelled(DatabaseError p1)
				{
					// TODO: Implement this method
				}
			});
	}
	
	public void channelDown(View view) {
		if (isPublic) {
			return;
		}
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		String currentChannel = read("current_channel", "channel_00_01");
		String a = currentChannel.substring(8, 10);
		String b = currentChannel.substring(11, 13);
		int aa = Integer.parseInt(a);
		int bb = Integer.parseInt(b);
		if (bb > 0) {
			bb--;
		} else {
			if (aa > 0) {
				bb = 99;
				aa -= 1;
			}
		}
		if (aa == 0 && bb == 0) {
			aa = 0;
			bb = 1;
		}
		currentChannel = "channel_"+String.format("%02d", aa)+"_"+String.format("%02d", bb);
		write("current_channel", currentChannel);
		show(currentChannel);
		String token = getToken(currentChannel);
		write("token", token);
		for (int i=0; i<pushCount; i++) {
			pop();
		}
		pushCount = 0;
		currentChannel = read("current_channel", "channel_00_01");
		worker().joinChannel(currentChannel, token, config().mUid);
		push();
		pushCount++;
		channelView.setText(currentChannel.replace("channel_", "").replace("_", ".").trim());
		searchingSignal = false;
		noSignal.setVisibility(View.GONE);
		searchingSignal = false;
	}
	
	/*public void channelDown(View view) {
		if (isPublic) {
			return;
		}
		if (!lcdActive) {
			return;
		}
		if (searchingSignal) {
			return;
		}
		searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		//show("Joining channel 2...");
		String currentChannel = read("current_channel", "channel_00_01");
		String a = currentChannel.substring(8, 10);
		String b = currentChannel.substring(11, 13);
		int aa = Integer.parseInt(a);
		int bb = Integer.parseInt(b);
		if (bb > 0) {
			bb--;
		} else {
			if (aa > 0) {
				bb = 99;
				aa -= 1;
			}
		}
		if (aa == 0 && bb == 0) {
			aa = 0;
			bb = 1;
		}
		currentChannel = "channel_"+String.format("%02d", aa)+"_"+String.format("%02d", bb);
		String prevChannel = getChannelName();
		write("current_channel", currentChannel);
		updateChannelCount(prevChannel, getChannelName());
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					write("token", token);
					final String currentChannel = read("current_channel", "channel_00_01");
					for (int i=0; i<pushCount; i++) {
						pop();
					}
					clearAll();
					pushCount = 0;
					ChannelMediaOptions opt = new ChannelMediaOptions();
					opt.autoSubscribeAudio = true;
					opt.autoSubscribeVideo = false;
					RtcChannel c = worker().getRtcEngine().createRtcChannel("channel_00_00");
					c.joinChannel(token, null, 0, opt);
					pushCount++;
					push(c);
					get(new Listener() {

							@Override
							public void onResponse(String token) {
								worker().joinChannel(currentChannel, token, 0);
								pushCount++;
								push();
								channelView.setText(currentChannel.replace("channel_", "").replace("_", ".").trim());
								searchingSignal = false;
								noSignal.setVisibility(View.GONE);
							}
						}, ConstantApp.TOKEN_GENERATION_URL+currentChannel);
				}
			}, ConstantApp.TOKEN_GENERATION_URL+"channel_00_00");
	}*/

	public void turnLCD(View view) {
		lcdActive = !lcdActive;
		if (lcdActive) {
			doConfigEngine(Constants.CLIENT_ROLE_AUDIENCE);
			lcd.setBackgroundResource(R.drawable.lcd_bg_active);
			noSignal.setBackgroundResource(R.drawable.lcd_bg_active);
			connectionStatusView.setVisibility(View.VISIBLE);
			FirebaseDatabase.getInstance().getReference("users").child(getDeviceID()).child("active").setValue(0);
			/*FirebaseDatabase.getInstance().getReference("user_count").addValueEventListener(new ValueEventListener() {

					@Override
					public void onDataChange(DataSnapshot dataSnapshot)
					{
						// TODO: Implement this method
						//show("Child count: "+dataSnapshot.getChildrenCount());
						String name = "";
						long userCount = 0;
						for (DataSnapshot snapshot2:dataSnapshot.getChildren()) {
							name = snapshot2.getKey();
							userCount = snapshot2.getChildrenCount();
						}
						String channelName = name;
						String currentChannelName = getChannelName();
						//show("Channel name: "+channelName+", current: "+currentChannelName+", user count: "+userCount);
						if (currentChannelName.equals(channelName)) {
							connectionCountView.setText(""+userCount);
						}
					}

					@Override
					public void onCancelled(DatabaseError p1)
					{
						// TODO: Implement this method
					}
				});*/
		} else {
			lcd.setBackgroundResource(R.drawable.lcd_bg_inactive);
			noSignal.setBackgroundResource(R.drawable.lcd_bg_inactive);
			connectionStatusView.setVisibility(View.GONE);
			connectionCountView.setText("0");
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void initUIandEvent() {
        event().addEventHandler(this);

        Intent i = getIntent();

        int cRole = i.getIntExtra(ConstantApp.ACTION_KEY_CROLE, 0);

        if (cRole == 0) {
            throw new RuntimeException("Should not reach here");
        }

        final String roomName = i.getStringExtra(ConstantApp.ACTION_KEY_ROOM_NAME);

        doConfigEngine(cRole);

        /*ImageView button1 = (ImageView) findViewById(R.id.switch_broadcasting_id);
		 ImageView button2 = (ImageView) findViewById(R.id.mute_local_speaker_id);

		 if (isBroadcaster(cRole)) {
		 broadcasterUI(button1, button2);
		 } else {
		 audienceUI(button1, button2);
		 }*/
		//show("UID: "+config().mUid);
		/*searchingSignal = true;
		noSignal.setVisibility(View.VISIBLE);
		final String currentChannel = read("current_channel", "channel_00_01");
		//show("Getting token...");
		get(new Listener() {

				@Override
				public void onResponse(String token) {
					//show(token);
					write("token", token);
					//show("Current channel: "+currentChannel+", token: "+token);
					isPublic = (read("is_public", 0)==1)?true:false;
					if (isPublic) {
						worker().joinChannel("channel_00_00", config().mUid);
					} else {
						worker().joinChannel(currentChannel, config().mUid);
					}
					updateChannelCount(null, currentChannel);
					channelView.setText(currentChannel.replace("channel_", "").replace("_", ".").trim());
					noSignal.setVisibility(View.GONE);
					searchingSignal = false;
				}
			}, ConstantApp.TOKEN_GENERATION_URL+currentChannel);*/

        /*TextView textRoomName = (TextView) findViewById(R.id.room_name);
		 textRoomName.setText(roomName);*/

        optional();
		changeToPrivate(null);

        /*LinearLayout bottomContainer = (LinearLayout) findViewById(R.id.bottom_container);
		 FrameLayout.MarginLayoutParams fmp = (FrameLayout.MarginLayoutParams) bottomContainer.getLayoutParams();
		 fmp.bottomMargin = virtualKeyHeight() + 16;*/
    }

    private Handler mMainHandler;

    private static final int UPDATE_UI_MESSAGE = 0x1024;

    //EditText mMessageList;

    StringBuffer mMessageCache = new StringBuffer();

    private void notifyMessageChanged(String msg) {
        if (mMessageCache.length() > 10000) { // drop messages
            mMessageCache = new StringBuffer(mMessageCache.substring(10000 - 40));
        }

        mMessageCache.append(System.currentTimeMillis()).append(": ").append(msg).append("\n"); // append timestamp for messages

        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (isFinishing()) {
						return;
					}

					if (mMainHandler == null) {
						mMainHandler = new Handler(getMainLooper()) {
							@Override
							public void handleMessage(Message msg) {
								super.handleMessage(msg);

								if (isFinishing()) {
									return;
								}

								switch (msg.what) {
									case UPDATE_UI_MESSAGE:
										String content = (String) (msg.obj);
										/*mMessageList.setText(content);
										 mMessageList.setSelection(content.length());*/
										break;

									default:
										break;
								}

							}
						};

						//mMessageList = (EditText) findViewById(R.id.msg_list);
					}

					mMainHandler.removeMessages(UPDATE_UI_MESSAGE);
					Message envelop = new Message();
					envelop.what = UPDATE_UI_MESSAGE;
					envelop.obj = mMessageCache.toString();
					mMainHandler.sendMessageDelayed(envelop, 1000l);
				}
			});
    }

    private void optional() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private void optionalDestroy() {
    }

    public void onSwitchSpeakerClicked(View view) {
        RtcEngine rtcEngine = rtcEngine();
		//rtcEngine.createRtcChannel("customchannel").joinChannel();
        rtcEngine.setEnableSpeakerphone(mAudioRouting != 3);
    }

    private void doConfigEngine(int cRole) {
        worker().configEngine(cRole);
    }

    private boolean isBroadcaster(int cRole) {
        return cRole == Constants.CLIENT_ROLE_BROADCASTER;
    }

    private boolean isBroadcaster() {
        return isBroadcaster(config().mClientRole);
    }

    @Override
    protected void deInitUIandEvent() {
        optionalDestroy();

        doLeaveChannel();
        event().removeEventHandler(this);
    }

    private void doLeaveChannel() {
        worker().leaveChannel(config().mChannel);
    }

    public void onEndCallClicked(View view) {
        quitCall();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        quitCall();
    }

    private void quitCall() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    private void doSwitchToBroadcaster(boolean broadcaster) {
        final int uid = config().mUid;


        if (broadcaster) {
            doConfigEngine(Constants.CLIENT_ROLE_BROADCASTER);

            new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {

						/*ImageView button1 = (ImageView) findViewById(R.id.switch_broadcasting_id);
						 ImageView button2 = (ImageView) findViewById(R.id.mute_local_speaker_id);
						 broadcasterUI(button1, button2);*/
					}
				}, 1000); // wait for reconfig engine
        } else {
            stopInteraction(uid);
        }
    }

    private void stopInteraction(final int uid) {
        doConfigEngine(Constants.CLIENT_ROLE_AUDIENCE);

        new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					ImageView button1 = (ImageView) findViewById(R.id.switch_broadcasting_id);
					ImageView button2 = (ImageView) findViewById(R.id.mute_local_speaker_id);
					audienceUI(button1, button2);
				}
			}, 1000); // wait for reconfig engine
    }

    private void audienceUI(ImageView button1, ImageView button2) {
        button1.setTag(null);
        button1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Object tag = v.getTag();
					if (tag != null && (boolean) tag) {
						doSwitchToBroadcaster(false);
					} else {
						doSwitchToBroadcaster(true);
					}
				}
			});
        button1.clearColorFilter();
        button2.setTag(null);
        button2.setVisibility(View.GONE);
        button2.clearColorFilter();
    }

    private void broadcasterUI(ImageView button1, ImageView button2) {
        button1.setTag(true);
        button1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Object tag = v.getTag();
					if (tag != null && (boolean) tag) {
						doSwitchToBroadcaster(false);
					} else {
						doSwitchToBroadcaster(true);
					}
				}
			});
        button1.setColorFilter(getResources().getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY);

        button2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Object tag = v.getTag();
					boolean flag = true;
					if (tag != null && (boolean) tag) {
						flag = false;
					}
					worker().getRtcEngine().muteLocalAudioStream(flag);
					/*ImageView button = (ImageView) v;
					 button.setTag(flag);
					 if (flag) {
					 button.setColorFilter(getResources().getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY);
					 } else {
					 button.clearColorFilter();
					 }*/
				}
			});

        button2.setVisibility(View.VISIBLE);
    }

    public void onVoiceMuteClicked(View view) {

        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.muteLocalAudioStream(mAudioMuted = !mAudioMuted);

        /*ImageView iv = (ImageView) view;

		 if (mAudioMuted) {
		 iv.setColorFilter(getResources().getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY);
		 } else {
		 iv.clearColorFilter();
		 }*/
    }

    @Override
    public void onExtraCallback(final int type, final Object... data) {

        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (isFinishing()) {
						return;
					}

					doHandleExtraCallback(type, data);
				}
			});
    }

    private void doHandleExtraCallback(int type, Object... data) {
        int peerUid;
        boolean muted;

        switch (type) {
            case AGEventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED: {
					peerUid = (Integer) data[0];
					muted = (boolean) data[1];

					notifyMessageChanged("mute: " + (peerUid & 0xFFFFFFFFL) + " " + muted);
					break;
				}

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_QUALITY: {
					peerUid = (Integer) data[0];
					int quality = (int) data[1];
					short delay = (short) data[2];
					short lost = (short) data[3];

					notifyMessageChanged("quality: " + (peerUid & 0xFFFFFFFFL) + " " + quality + " " + delay + " " + lost);
					break;
				}

            case AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS: {
					IRtcEngineEventHandler.AudioVolumeInfo[] infos = (IRtcEngineEventHandler.AudioVolumeInfo[]) data[0];

					if (infos.length == 1 && infos[0].uid == 0) { // local guy, ignore it
						break;
					}

					StringBuilder volumeCache = new StringBuilder();
					for (IRtcEngineEventHandler.AudioVolumeInfo each : infos) {
						peerUid = each.uid;
						int peerVolume = each.volume;

						if (peerUid == 0) {
							continue;
						}

						volumeCache.append("volume: ").append(peerUid & 0xFFFFFFFFL).append(" ").append(peerVolume).append("\n");
					}

					if (volumeCache.length() > 0) {
						String volumeMsg = volumeCache.substring(0, volumeCache.length() - 1);
						notifyMessageChanged(volumeMsg);

						if ((System.currentTimeMillis() / 1000) % 10 == 0) {

						}
					}
					break;
				}

            case AGEventHandler.EVENT_TYPE_ON_APP_ERROR: {
					int subType = (int) data[0];

					if (subType == ConstantApp.AppError.NO_NETWORK_CONNECTION) {
						showLongToast(getString(R.string.msg_no_network_connection));
					}

					break;
				}

            case AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR: {
					int error = (int) data[0];
					String description = (String) data[1];

					notifyMessageChanged(error + " " + description);

					break;
				}

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED: {
					notifyHeadsetPlugged((int) data[0]);

					break;
				}
        }
    }

    public void notifyHeadsetPlugged(final int routing) {

        mAudioRouting = routing;

        /*ImageView iv = (ImageView) findViewById(R.id.switch_speaker_id);
		 if (mAudioRouting == 3) { // Speakerphone
		 iv.setColorFilter(getResources().getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY);
		 } else {
		 iv.clearColorFilter();
		 }*/
    }




	public void onClientRoleChanged(int oldRole, int newRole) {
		Util.show(mContext, "onClientRoleChanged");
		/*Util.show(mContext, "onClientRoleChanged()");
		 if (oldRole == Constants.CLIENT_ROLE_AUDIENCE && newRole == Constants.CLIENT_ROLE_BROADCASTER) {
		 MediaPlayer mp = MediaPlayer.create(mContext, R.raw.tonestart);
		 mp.start();
		 } else if (oldRole == Constants.CLIENT_ROLE_BROADCASTER && newRole == Constants.CLIENT_ROLE_AUDIENCE) {
		 MediaPlayer mp = MediaPlayer.create(mContext, R.raw.toneend);
		 mp.start();
		 }*/
	}

	public void onLocalUserRegistered(int uid, java.lang.String userAccount) {
		Util.show(mContext, "onLocalUserRegistered()");
	}

	public void onUserInfoUpdated(int uid, io.agora.rtc.models.UserInfo userInfo) {
		Util.show(mContext, "onUserInfoUpdated");
	}

	public void onConnectionStateChanged(int state, int reason) {
		Util.show(mContext, "onConnectionStateChanged");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onConnectionBanned() {
		Util.show(mContext, "onConnectionBanned");
	}

	public void onApiCallExecuted(int error, java.lang.String api, java.lang.String result) {
		Util.show(mContext, "onApiCallExecuted");
	}

	public void onTokenPrivilegeWillExpire(java.lang.String token) {
		Util.show(mContext, "onTokenPrivilegeWillExpire");
	}

	public void onRequestToken() {
		Util.show(mContext, "onRequestToken");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onMicrophoneEnabled(boolean enabled) {
		Util.show(mContext, "onMicrophoneEnabled");
	}

	public void onActiveSpeaker(int uid) {
		Util.show(mContext, "onActiveSpeaker");
	}

	public void onFirstLocalAudioFrame(int elapsed) {
		Util.show(mContext, "onFirstLocalAudioFrame");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onFirstRemoteAudioFrame(int uid, int elapsed) {
		Util.show(mContext, "onFirstRemoteAudioFrame()");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onVideoStopped() {
		Util.show(mContext, "onVideoStopped");
	}

	public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
		Util.show(mContext, "onFirstLocalVideoFrame");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
		Util.show(mContext, "onFirstRemoteVideoDecoded");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
		Util.show(mContext, "onFirstRemoteVideoFrame");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onUserMuteAudio(int uid, boolean muted) {
		Util.show(mContext, "onUserMuteAudio");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onUserMuteVideo(int uid, boolean muted) {
		Util.show(mContext, "onUserMuteVideo");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onUserEnableVideo(int uid, boolean enabled) {
		Util.show(mContext, "onUserEnableVideo");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onUserEnableLocalVideo(int uid, boolean enabled) {
		Util.show(mContext, "onUserEnableLocalVideo");
	}

	public void onVideoSizeChanged(int uid, int width, int height, int rotation) {
		Util.show(mContext, "onVideoSizeChanged");
	}

	public void onRemoteAudioStateChanged(int uid, int state, int reason, int elapsed) {
		Util.show(mContext, "onRemoteAudioStateChanged");
	}

	public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
		Util.show(mContext, "onRemoteVideoStateChanged");
	}

	public void onChannelMediaRelayStateChanged(int state, int code) {
		Util.show(mContext, "onChannelMediaRelayStateChanged");
	}

	public void onChannelMediaRelayEvent(int code) {
		Util.show(mContext, "onChannelMediaRelayEvent");
	}

	public void onLocalPublishFallbackToAudioOnly(boolean isFallbackOrRecover) {
		Util.show(mContext, "onLocalPublishFallbackToAudioOnly");
	}

	public void onRemoteSubscribeFallbackToAudioOnly(int uid, boolean isFallbackOrRecover) {
		Util.show(mContext, "onRemoteSubscribeFallbackToAudioOnly");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onCameraReady() {
		Util.show(mContext, "onCameraReady");
	}

	public void onCameraFocusAreaChanged(android.graphics.Rect rect) {
		Util.show(mContext, "onCameraFocusAreaChanged");
	}

	public void onCameraExposureAreaChanged(android.graphics.Rect rect) {
		Util.show(mContext, "onCameraExposureAreaChanged");
	}

	public void onLastmileProbeResult(io.agora.rtc.IRtcEngineEventHandler.LastmileProbeResult result) {
		Util.show(mContext, "onLastmileProbeResult");
	}

	public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
		Util.show(mContext, "onNetworkQuality");
	}

	public void onLocalVideoStats(io.agora.rtc.IRtcEngineEventHandler.LocalVideoStats stats) {
		Util.show(mContext, "onLocalVideoStats");
	}

	public void onRemoteVideoStats(io.agora.rtc.IRtcEngineEventHandler.RemoteVideoStats stats) {
		Util.show(mContext, "onRemoteVideoStats");
	}

	public void onLocalAudioStats(io.agora.rtc.IRtcEngineEventHandler.LocalAudioStats stats) {
		Util.show(mContext, "onLocalAudioStats");
	}

	public void onRemoteAudioStats(io.agora.rtc.IRtcEngineEventHandler.RemoteAudioStats stats) {
		Util.show(mContext, "onRemoteAudioStats");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onLocalVideoStat(int sentBitrate, int sentFrameRate) {
		Util.show(mContext, "onLocalVideoStat");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onRemoteVideoStat(int uid, int delay, int receivedBitrate, int receivedFrameRate) {
		Util.show(mContext, "onRemoteVideoStat");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onRemoteAudioTransportStats(int uid, int delay, int lost, int rxKBitRate) {
		Util.show(mContext, "onRemoteAudioTransportStats");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onRemoteVideoTransportStats(int uid, int delay, int lost, int rxKBitRate) {
		Util.show(mContext, "onRemoteVideoTransportStats");
	}

	public void onAudioMixingStateChanged(int state, int errorCode) {
		Util.show(mContext, "onAudioMixingStateChanged");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onAudioMixingFinished() {
		Util.show(mContext, "onAudioMixingFinished");
	}

	public void onAudioEffectFinished(int soundId) {
		Util.show(mContext, "onAudioEffectFinished");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onFirstRemoteAudioDecoded(int uid, int elapsed) {
		Util.show(mContext, "onFirstRemoteAudioDecoded");
	}

	public void onLocalAudioStateChanged(int state, int error) {
		Util.show(mContext, "onLocalAudioStateChanged");
	}

	public void onLocalVideoStateChanged(int localVideoState, int error) {}

	public void onRtmpStreamingStateChanged(java.lang.String url, int state, int errCode) {
		Util.show(mContext, "onRtmpStreamingStateChanged");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onStreamPublished(java.lang.String url, int error) {
		Util.show(mContext, "onStreamPublished");
	}

	/** @deprecated */
	@java.lang.Deprecated()
	public void onStreamUnpublished(java.lang.String url) {
		Util.show(mContext, "onStreamUnpublished");
	}

	public void onTranscodingUpdated() {
		Util.show(mContext, "onTranscodingUpdated");
	}

	public void onStreamInjectedStatus(java.lang.String url, int uid, int status) {
		Util.show(mContext, "onStreamInjectedStatus");
	}

	public void onStreamMessage(int uid, int streamId, byte[] data) {
		Util.show(mContext, "onStreamMessage");
	}

	public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached) {
		Util.show(mContext, "onStreamMessageError");
	}

	public void onMediaEngineLoadSuccess() {
		Util.show(mContext, "onMediaEngineLoadSuccess");
	}

	public void onMediaEngineStartCallSuccess() {
		Util.show(mContext, "onMediaEngineStartCallSuccess");
	}

	public void onNetworkTypeChanged(int type) {
		Util.show(mContext, "onNetworkTypeChanged");
	}

	@Override
	public void onUserJoined(int uid, int elapsed) {
		Util.show(mContext, "User joined");
	}

	@Override
	public void onUserOffline(int uid, int reason) {
		Util.show(mContext, "onUserOffline");
		// FIXME this callback may return times
		/*Iterator<AGEventHandler> it = mEventHandlerList.keySet().iterator();
		 while (it.hasNext()) {
		 AGEventHandler handler = it.next();
		 handler.onUserOffline(uid, reason);
		 }*/
	}

	@Override
	public void onRtcStats(RtcStats stats) {
		Util.show(mContext, "onRtcStats");
	}

	@Override
	public void onAudioVolumeIndication(AudioVolumeInfo[] speakerInfos, int totalVolume) {
		Util.show(mContext, "onAudioVolumeIndication");
		if (speakerInfos == null) {
			// quick and dirty fix for crash
			// TODO should reset UI for no sound
			return;
		}

		/*Iterator<AGEventHandler> it = mEventHandlerList.keySet().iterator();
		 while (it.hasNext()) {
		 AGEventHandler handler = it.next();
		 handler.onExtraCallback(AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS, (Object) speakerInfos);
		 }*/
	}

	@Override
	public void onLeaveChannel(RtcStats stats) {
		Util.show(mContext, "On leave channel");
	}

	@Override
	public void onLastmileQuality(int quality) {
		Util.show(mContext, "onLastmileQuality");
	}

	@Override
	public void onError(int error) {
		Util.show(mContext, "onError");
		/*Iterator<AGEventHandler> it = mEventHandlerList.keySet().iterator();
		 while (it.hasNext()) {
		 AGEventHandler handler = it.next();
		 handler.onExtraCallback(AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR, error, RtcEngine.getErrorDescription(error));
		 }*/
	}

	@Override
	public void onAudioQuality(int uid, int quality, short delay, short lost) {
		Util.show(mContext, "onAudioQuality");
		/*Iterator<AGEventHandler> it = mEventHandlerList.keySet().iterator();
		 while (it.hasNext()) {
		 AGEventHandler handler = it.next();
		 handler.onExtraCallback(AGEventHandler.EVENT_TYPE_ON_AUDIO_QUALITY, uid, quality, delay, lost);
		 }*/
	}

	@Override
	public void onConnectionInterrupted() {
		Util.show(mContext, "onConnectionInterrupted");
		/*Iterator<AGEventHandler> it = mEventHandlerList.keySet().iterator();
		 while (it.hasNext()) {
		 AGEventHandler handler = it.next();
		 handler.onExtraCallback(AGEventHandler.EVENT_TYPE_ON_APP_ERROR, ConstantApp.AppError.NO_NETWORK_CONNECTION);
		 }*/
	}

	@Override
	public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
		Util.show(mContext, "onJoinChannelSuccess");
		/*mConfig.mUid = uid;
		 //Toast.makeText(mContext, "Join channel success, channel: "+channel+", uid: "+uid, Toast.LENGTH_LONG).show();
		 Iterator<AGEventHandler> it = mEventHandlerList.keySet().iterator();
		 while (it.hasNext()) {
		 AGEventHandler handler = it.next();
		 handler.onJoinChannelSuccess(channel, uid, elapsed);
		 }*/
	}

	public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
		Util.show(mContext, "onRejoinChannelSuccess");
	}

	public void onWarning(int warn) {
		Util.show(mContext, "onWarning");
	}

	@Override
	public void onAudioRouteChanged(int routing) {
		Util.show(mContext, "onAudioRouteChanged");
		/*Iterator<AGEventHandler> it = mEventHandlerList.keySet().iterator();
		 while (it.hasNext()) {
		 AGEventHandler handler = it.next();
		 handler.onExtraCallback(AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED, routing);
		 }*/

	}
}

