package io.agora.openlive.voice.only.ui;

import android.content.SharedPreferences;
import android.content.Context;
import android.widget.Toast;

public class Util
 {
	
	public static String read(Context ctx, String name, String defaultValue) {
		SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
		return sp.getString(name, defaultValue);
	}

	public static void write(Context ctx, String name, String value) {
		SharedPreferences sp = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
		SharedPreferences.Editor e = sp.edit();
		e.putString(name, value);
		e.commit();
	}
	
	public static void show(Context ctx, String message) {
		Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
	}
}
