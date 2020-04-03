package io.agora.openlive.voice.only.model;

public class ConstantApp {
    public static final String APP_BUILD_DATE = "today";
	public static final String TOKEN_GENERATION_URL = "http://danaos-preview.000webhostapp.com/tokenbuilder/sample/a.php?channel_name=";
    public static final int BASE_VALUE_PERMISSION = 0X0001;
    public static final int PERMISSION_REQ_ID_RECORD_AUDIO = BASE_VALUE_PERMISSION + 1;
    public static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = BASE_VALUE_PERMISSION + 3;

    public static class PrefManager {
        public static final String PREF_PROPERTY_UID = "pOCXx_uid";
    }

    public static final String ACTION_KEY_CROLE = "C_Role";
    public static final String ACTION_KEY_ROOM_NAME = "ecHANEL";
	//public static final String TOKEN = "0067d07a0e18f6b49cca8b0d108917f414cIAAUl7fifyf8HOdePawF1LPpy8mIuCh70u8Tel8T4DXDXhlGflwAAAAAIgCAbwAAbMWGXgQAAQD8gYVeAwD8gYVeAgD8gYVeBAD8gYVe";

    public static class AppError {
        public static final int NO_NETWORK_CONNECTION = 3;
    }
}
