package io.agora.openlive.voice.only.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import com.prod.walkietalkie.R;
import io.agora.rtc.models.ChannelMediaOptions;
import android.widget.Toast;
import io.agora.openlive.voice.only.ui.Util;

public class WorkerThread extends Thread {
    
    private final Context mContext;

    private static final int ACTION_WORKER_THREAD_QUIT = 0X1010; // quit this thread

    private static final int ACTION_WORKER_JOIN_CHANNEL = 0X2010;

    private static final int ACTION_WORKER_LEAVE_CHANNEL = 0X2011;

    private static final int ACTION_WORKER_CONFIG_ENGINE = 0X2012;

    private static final class WorkerThreadHandler extends Handler {

        private WorkerThread mWorkerThread;

        WorkerThreadHandler(WorkerThread thread) {
            this.mWorkerThread = thread;
        }

        public void release() {
            mWorkerThread = null;
        }

        @Override
        public void handleMessage(Message msg) {
            if (this.mWorkerThread == null) {
                
                return;
            }

            switch (msg.what) {
                case ACTION_WORKER_THREAD_QUIT:
                    mWorkerThread.exit();
                    break;
                case ACTION_WORKER_JOIN_CHANNEL:
                    String[] data = (String[]) msg.obj;
                    mWorkerThread.joinChannel(data[0], msg.arg1);
                    break;
                case ACTION_WORKER_LEAVE_CHANNEL:
                    String channel = (String) msg.obj;
                    mWorkerThread.leaveChannel(channel);
                    break;
                case ACTION_WORKER_CONFIG_ENGINE:
                    Object[] configData = (Object[]) msg.obj;
                    mWorkerThread.configEngine((int) configData[0]);
                    break;
            }
        }
    }

    private WorkerThreadHandler mWorkerHandler;

    private boolean mReady;

    public final void waitForReady() {
        while (!mReady) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }
    }

    @Override
    public void run() {
        
        Looper.prepare();

        mWorkerHandler = new WorkerThreadHandler(this);

        ensureRtcEngineReadyLock();

        mReady = true;

        // enter thread looper
        Looper.loop();
    }

    private RtcEngine mRtcEngine;

    public final void joinChannel(final String channel, int uid) {
        if (Thread.currentThread() != this) {
            
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_JOIN_CHANNEL;
            envelop.obj = new String[]{channel};
            envelop.arg1 = uid;
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        ensureRtcEngineReadyLock();
		//0067d07a0e18f6b49cca8b0d108917f414cIACiArBFuPsrzOhqMHEU2AGW8daXdxGef9j+t1XJiI70VObgcckAAAAAIgBWsgAAGc+GXgQAAQCpi4VeAwCpi4VeAgCpi4VeBACpi4Ve
		//Util.show(mContext, "Token (2): "+Util.read(mContext, "token", ""));
        mRtcEngine.joinChannel(Util.read(mContext, "token", ""), channel, "OpenVCall", uid);
		//mRtcEngine.joinChannel("0067d07a0e18f6b49cca8b0d108917f414cIACiArBFuPsrzOhqMHEU2AGW8daXdxGef9j+t1XJiI70VObgcckAAAAAIgBWsgAAGc+GXgQAAQCpi4VeAwCpi4VeAgCpi4VeBACpi4Ve", channel, "OpenVCall", uid);
		//mRtcEngine.createRtcChannel("ChannelOne");
		// Create second channel
		/*ChannelMediaOptions mediaOptions = new ChannelMediaOptions();
        mediaOptions.autoSubscribeAudio = true;
        mediaOptions.autoSubscribeVideo = true;
		int ret = mRtcEngine.createRtcChannel("ChannelOne").joinChannel(ConstantApp.TOKEN, channel, uid, mediaOptions);
		//Toast.makeText(mContext, "Create RTC channel: "+ret, Toast.LENGTH_LONG).show();*/
        mEngineConfig.mChannel = channel;
    }
	
	public final void joinChannel(final String channel, final String token, int uid) {
        if (Thread.currentThread() != this) {

            Message envelop = new Message();
            envelop.what = ACTION_WORKER_JOIN_CHANNEL;
            envelop.obj = new String[]{channel};
            envelop.arg1 = uid;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        ensureRtcEngineReadyLock();
		//0067d07a0e18f6b49cca8b0d108917f414cIACiArBFuPsrzOhqMHEU2AGW8daXdxGef9j+t1XJiI70VObgcckAAAAAIgBWsgAAGc+GXgQAAQCpi4VeAwCpi4VeAgCpi4VeBACpi4Ve
		//Util.show(mContext, "Token (2): "+Util.read(mContext, "token", ""));
        mRtcEngine.joinChannel(token, channel, "OpenVCall", uid);
		//mRtcEngine.joinChannel("0067d07a0e18f6b49cca8b0d108917f414cIACiArBFuPsrzOhqMHEU2AGW8daXdxGef9j+t1XJiI70VObgcckAAAAAIgBWsgAAGc+GXgQAAQCpi4VeAwCpi4VeAgCpi4VeBACpi4Ve", channel, "OpenVCall", uid);
		//mRtcEngine.createRtcChannel("ChannelOne");
		// Create second channel
		/*ChannelMediaOptions mediaOptions = new ChannelMediaOptions();
		 mediaOptions.autoSubscribeAudio = true;
		 mediaOptions.autoSubscribeVideo = true;
		 int ret = mRtcEngine.createRtcChannel("ChannelOne").joinChannel(ConstantApp.TOKEN, channel, uid, mediaOptions);
		 //Toast.makeText(mContext, "Create RTC channel: "+ret, Toast.LENGTH_LONG).show();*/
        mEngineConfig.mChannel = channel;
    }

    public final void leaveChannel(String channel) {
        if (Thread.currentThread() != this) {
            
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_LEAVE_CHANNEL;
            envelop.obj = channel;
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }

        mEngineConfig.reset();
        
    }

    private EngineConfig mEngineConfig;

    public final EngineConfig getEngineConfig() {
        return mEngineConfig;
    }

    private final MyEngineEventHandler mEngineEventHandler;

    public final void configEngine(int cRole) {
        if (Thread.currentThread() != this) {
            
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_CONFIG_ENGINE;
            envelop.obj = new Object[]{cRole};
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        ensureRtcEngineReadyLock();
        mEngineConfig.mClientRole = cRole;
        mRtcEngine.setClientRole(cRole);

        
    }

    public static String getDeviceID(Context context) {
        // XXX according to the API docs, this value may change after factory reset
        // use Android id as device id
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private RtcEngine ensureRtcEngineReadyLock() {
        if (mRtcEngine == null) {
            String appId = mContext.getString(R.string.private_app_id);
            if (TextUtils.isEmpty(appId)) {
                throw new RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/");
            }
            try {
                mRtcEngine = RtcEngine.create(mContext, appId, mEngineEventHandler.mRtcEventHandler);
            } catch (Exception e) {
                
                throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
            }
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            mRtcEngine.enableAudioVolumeIndication(200, 3, false); // 200 ms
            mRtcEngine.setLogFile(Environment.getExternalStorageDirectory()
                    + File.separator + mContext.getPackageName() + "/log/agora-rtc.log");
        }
        return mRtcEngine;
    }

    public MyEngineEventHandler eventHandler() {
        return mEngineEventHandler;
    }

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    /**
     * call this method to exit
     * should ONLY call this method when this thread is running
     */
    public final void exit() {
        if (Thread.currentThread() != this) {
            
            mWorkerHandler.sendEmptyMessage(ACTION_WORKER_THREAD_QUIT);
            return;
        }

        mReady = false;

        // TODO should remove all pending(read) messages

        

        // exit thread looper
        Looper.myLooper().quit();

        mWorkerHandler.release();

        
    }

    public WorkerThread(Context context) {
        this.mContext = context;

        this.mEngineConfig = new EngineConfig();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        this.mEngineConfig.mUid = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_UID, 0);

        this.mEngineEventHandler = new MyEngineEventHandler(mContext, this.mEngineConfig);
    }
}
