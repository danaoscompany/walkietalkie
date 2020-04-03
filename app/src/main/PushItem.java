package com.prod.walkietalkie;

import io.agora.rtc.RtcChannel;

public class PushItem {
	public static final int TYPE_ENGINE = 1;
	public static final int TYPE_CHANNEL = 2;
	public int type = 0;
	public RtcChannel channel = null;
}
