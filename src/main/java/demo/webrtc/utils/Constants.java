package demo.webrtc.utils;

import demo.webrtc.model.StreamSession;
import org.kurento.client.KurentoClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kantipud on 21-12-2017.
 */
public class Constants {

    private static final Map<String, StreamSession> streamSessionMap = new HashMap<>();
    private static final KurentoClient KURENTO_CLIENT = KurentoClient.create("ws://10.142.138.165:8888/kurento");
    public static final String DEFUALT_HTTP_PORT = "9876";
    public static final String RTSP_URL = "rtspUrl";
    public static final String CLIENT_HOST = "clientHost";
    public static final String MISSION_ID_JSONKEY = "missionId";
    public static final String DEVICE_ID_JSONKEY = "deviceId";
    public static final String NETWORK_CACHE = "networkCache";
    public static final String USE_ENCODED_MEDIA = "useEncodedMedia";

    public static void addStreamSession(String streamId, StreamSession streamSession) {
        streamSessionMap.put(streamId, streamSession);
    }

    public static Map<String, StreamSession> getStreamSessionMap(){
        return streamSessionMap;
    }

    public static StreamSession getStreamSession(String streamId) {
        return streamSessionMap.get(streamId);
    }

    public static StreamSession removeStreamSession(String streamId) {
        return streamSessionMap.remove(streamId);
    }

    public static void releaseStreamSession(StreamSession streamSession) {
        streamSession.getMediaPipeline().release();
    }

    public static KurentoClient getKurentoClient(){
        return KURENTO_CLIENT;
    }
}
