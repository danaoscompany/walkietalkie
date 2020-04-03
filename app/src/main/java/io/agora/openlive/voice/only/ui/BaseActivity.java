package io.agora.openlive.voice.only.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Toast;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import io.agora.openlive.voice.only.model.ConstantApp;
import io.agora.openlive.voice.only.model.CurrentUserSettings;
import io.agora.openlive.voice.only.model.EngineConfig;
import io.agora.openlive.voice.only.model.MyEngineEventHandler;
import io.agora.openlive.voice.only.model.WorkerThread;
import io.agora.openlive.voice.only.ui.AGApplication;
import io.agora.propeller.Constant;
import io.agora.rtc.BuildConfig;
import io.agora.rtc.RtcEngine;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;
import android.content.SharedPreferences;
import java.util.Arrays;
import com.squareup.okhttp.Protocol;

public abstract class BaseActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View layout = findViewById(Window.ID_ANDROID_CONTENT);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                initUIandEvent();
            }
        });
    }

    protected abstract void initUIandEvent();

    protected abstract void deInitUIandEvent();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                boolean checkPermissionResult = checkSelfPermissions();

                if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {
                    // so far we do not use OnRequestPermissionsResultCallback
                }
            }
        }, 500);
    }

    private boolean checkSelfPermissions() {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO, ConstantApp.PERMISSION_REQ_ID_RECORD_AUDIO) &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, ConstantApp.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onDestroy() {
        deInitUIandEvent();
        super.onDestroy();
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        
        /*if (checkSelfPermission(
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{permission},
                    requestCode);
            return false;
        }*/

        if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
            ((AGApplication) getApplication()).initWorkerThread();
        }
        return true;
    }

    protected RtcEngine rtcEngine() {
        return ((AGApplication) getApplication()).getWorkerThread().getRtcEngine();
    }

    protected final WorkerThread worker() {
        return ((AGApplication) getApplication()).getWorkerThread();
    }

    protected final EngineConfig config() {
        return ((AGApplication) getApplication()).getWorkerThread().getEngineConfig();
    }

    protected final MyEngineEventHandler event() {
        return ((AGApplication) getApplication()).getWorkerThread().eventHandler();
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        
        switch (requestCode) {
            case ConstantApp.PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, ConstantApp.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
                    ((AGApplication) getApplication()).initWorkerThread();
                } else {
                    finish();
                }
                break;
            }
            case ConstantApp.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                }
                break;
            }
        }
    }

    protected CurrentUserSettings vSettings() {
        return AGApplication.mAudioSettings;
    }

    protected int virtualKeyHeight() {
        boolean hasPermanentMenuKey = false;//ViewConfigurationCompat.hasPermanentMenuKey(ViewConfiguration.get(getApplication()));

        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(metrics);
        } else {
            display.getMetrics(metrics);
        }

        int fullHeight = metrics.heightPixels;

        display.getMetrics(metrics);

        return fullHeight - metrics.heightPixels;
    }

    protected void initVersionInfo() {
        String version = "V " + BuildConfig.VERSION_NAME + "(Build: " + BuildConfig.VERSION_CODE
                + ", " + ConstantApp.APP_BUILD_DATE + ", SDK: " + Constant.MEDIA_SDK_VERSION + ")";
//        TextView textVersion = (TextView) findViewById(R.id.app_version);
//        textVersion.setText(version);
    }
	
	public void show(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	public void show(int messageID) {
		Toast.makeText(this, messageID, Toast.LENGTH_LONG).show();
	}
	
	public String getDeviceID() {
		String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		return deviceID;
	}
	
	public void run(Runnable runnable) {
		new Thread(runnable).start();
	}

	public void runLater(Runnable runnable) {
		new Handler(Looper.getMainLooper()).post(runnable);
	}

	public OkHttpClient getOkHttpClient() {
		OkHttpClient client = new OkHttpClient();
		client.setReadTimeout(60, TimeUnit.SECONDS);
		client.setWriteTimeout(60, TimeUnit.SECONDS);
		client.setConnectTimeout(60, TimeUnit.SECONDS);
		client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
		return client;
	}

	public void get(final Listener listener, final String url) {
		run(new Runnable() {

				public void run() {
					try {
						OkHttpClient client = getOkHttpClient();
						Request request = new Request.Builder()
							.url(url)
							.build();
						final String response = client.newCall(request).execute().body().string();
						runLater(new Runnable() {

								public void run() {
									//log("Response: "+response);
									if (listener != null) {
										listener.onResponse(response);
									}
								}
							});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
	}

	public void post(final Listener listener, final String url, final String ...params) {
		run(new Runnable() {

				public void run() {
					try {
						OkHttpClient client = getOkHttpClient();
						MultipartBuilder builder = new MultipartBuilder()
							.type(MultipartBuilder.FORM);
						for (int i=0; i<params.length;) {
							if (params[i].equals("file")) {
								String fileName = params[i+1];
								String mimeType = params[i+2];
								String filePath = params[i+3];
								builder.addFormDataPart("file", fileName,
														RequestBody.create(MediaType.parse(mimeType), new File(filePath)));
								i += 4;
							} else {
								builder.addFormDataPart(params[i], params[i+1]);
								i += 2;
							}
						}
						Request request = new Request.Builder()
							.url(url)
							.post(builder.build())
							.build();
						final String response = client.newCall(request).execute().body().string();
						runLater(new Runnable() {

								public void run() {
									//log("Response: "+response);
									if (listener != null) {
										listener.onResponse(response);
									}
								}
							});
					} catch (final Exception e) {
						e.printStackTrace();
						runLater(new Runnable() {

								@Override
								public void run()
								{
									// TODO: Implement this method
									show(e.getMessage());
								}


							});
					}
				}
			});
	}

	public interface Listener {

		void onResponse(String response);
	}
	
	public String read(String name, String defaultValue) {
		SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
		return sp.getString(name, defaultValue);
	}
	
	public int read(String name, int defaultValue) {
		SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
		return sp.getInt(name, defaultValue);
	}
	
	public void write(String name, String value) {
		SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
		SharedPreferences.Editor e = sp.edit();
		e.putString(name, value);
		e.commit();
	}
	
	public void write(String name, int value) {
		SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
		SharedPreferences.Editor e = sp.edit();
		e.putInt(name, value);
		e.commit();
	}
}
