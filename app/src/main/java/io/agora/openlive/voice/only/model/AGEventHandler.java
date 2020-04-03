package io.agora.openlive.voice.only.model;

public interface AGEventHandler {
	
    void onJoinChannelSuccess(String channel, int uid, int elapsed);

    void onUserOffline(int uid, int reason);

    void onExtraCallback(int type, Object... data);
	
	public void onWarning(int warn);

    public void onError(int err);

    public void onRejoinChannelSuccess(java.lang.String channel, int uid, int elapsed);

    public void onLeaveChannel(io.agora.rtc.IRtcEngineEventHandler.RtcStats stats);

    public void onClientRoleChanged(int oldRole, int newRole);

    public void onLocalUserRegistered(int uid, java.lang.String userAccount);

    public void onUserInfoUpdated(int uid, io.agora.rtc.models.UserInfo userInfo);

    public void onUserJoined(int uid, int elapsed);

    public void onConnectionStateChanged(int state, int reason);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onConnectionInterrupted();

    public void onApiCallExecuted(int error, java.lang.String api, java.lang.String result);

    public void onTokenPrivilegeWillExpire(java.lang.String token);

    @java.lang.Deprecated()
    public void onMicrophoneEnabled(boolean enabled);

    public void onAudioVolumeIndication(io.agora.rtc.IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume);

    public void onFirstLocalAudioFrame(int elapsed);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onFirstRemoteAudioFrame(int uid, int elapsed);

    public void onFirstLocalVideoFrame(int width, int height, int elapsed);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onUserMuteAudio(int uid, boolean muted);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onUserMuteVideo(int uid, boolean muted);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onUserEnableVideo(int uid, boolean enabled);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onUserEnableLocalVideo(int uid, boolean enabled);

    public void onVideoSizeChanged(int uid, int width, int height, int rotation);

    public void onRemoteAudioStateChanged(int uid, int state, int reason, int elapsed);

    public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed);

    public void onChannelMediaRelayStateChanged(int state, int code);

    public void onChannelMediaRelayEvent(int code);

    public void onLocalPublishFallbackToAudioOnly(boolean isFallbackOrRecover);

    public void onRemoteSubscribeFallbackToAudioOnly(int uid, boolean isFallbackOrRecover);

    public void onAudioRouteChanged(int routing);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onCameraReady();

    public void onCameraFocusAreaChanged(android.graphics.Rect rect);

    public void onCameraExposureAreaChanged(android.graphics.Rect rect);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onAudioQuality(int uid, int quality, short delay, short lost);

    public void onRtcStats(io.agora.rtc.IRtcEngineEventHandler.RtcStats stats);

    public void onLastmileQuality(int quality);

    public void onLastmileProbeResult(io.agora.rtc.IRtcEngineEventHandler.LastmileProbeResult result);

    public void onNetworkQuality(int uid, int txQuality, int rxQuality);

    public void onLocalVideoStats(io.agora.rtc.IRtcEngineEventHandler.LocalVideoStats stats);

    public void onRemoteVideoStats(io.agora.rtc.IRtcEngineEventHandler.RemoteVideoStats stats);

    public void onLocalAudioStats(io.agora.rtc.IRtcEngineEventHandler.LocalAudioStats stats);

    public void onRemoteAudioStats(io.agora.rtc.IRtcEngineEventHandler.RemoteAudioStats stats);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onLocalVideoStat(int sentBitrate, int sentFrameRate);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onRemoteVideoStat(int uid, int delay, int receivedBitrate, int receivedFrameRate);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onRemoteAudioTransportStats(int uid, int delay, int lost, int rxKBitRate);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onRemoteVideoTransportStats(int uid, int delay, int lost, int rxKBitRate);

    public void onAudioMixingStateChanged(int state, int errorCode);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onAudioMixingFinished();

    public void onAudioEffectFinished(int soundId);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onFirstRemoteAudioDecoded(int uid, int elapsed);

    public void onLocalAudioStateChanged(int state, int error);

    public void onLocalVideoStateChanged(int localVideoState, int error);

    public void onRtmpStreamingStateChanged(java.lang.String url, int state, int errCode);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onStreamPublished(java.lang.String url, int error);

    /** @deprecated */
    @java.lang.Deprecated()
    public void onStreamUnpublished(java.lang.String url);

    public void onTranscodingUpdated();

    public void onStreamInjectedStatus(java.lang.String url, int uid, int status);

    public void onStreamMessage(int uid, int streamId, byte[] data);

    public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached);

    public void onMediaEngineLoadSuccess();

    public void onMediaEngineStartCallSuccess();

    public void onNetworkTypeChanged(int type);

    int EVENT_TYPE_ON_USER_AUDIO_MUTED = 7;

    int EVENT_TYPE_ON_SPEAKER_STATS = 8;

    int EVENT_TYPE_ON_AGORA_MEDIA_ERROR = 9;

    int EVENT_TYPE_ON_AUDIO_QUALITY = 10;

    int EVENT_TYPE_ON_APP_ERROR = 13;

    int EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED = 18;
}
