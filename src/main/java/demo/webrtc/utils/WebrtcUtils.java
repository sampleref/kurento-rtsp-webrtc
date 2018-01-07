package demo.webrtc.utils;

import demo.webrtc.model.StreamSession;
import org.apache.logging.log4j.LogManager;
import org.kurento.client.WebRtcEndpoint;

import java.util.UUID;

/**
 * Created by kantipud on 27-12-2017.
 */
public class WebrtcUtils {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();

    public static void removeMatchingSocketSessionId(String sessionId) {
        StreamSession streamSessionMatch = null;
        for (StreamSession streamSession : Constants.getStreamSessionMap().values()) {
            WebRtcEndpoint webRtcEndpoint = streamSession.getWebRtcEndpointsConnected().get(sessionId);
            if (webRtcEndpoint != null) {
                streamSessionMatch = streamSession;
                break;
            }
        }
        if (streamSessionMatch != null && streamSessionMatch.getWebRtcEndpointsConnected().get(sessionId) != null) {
            streamSessionMatch.getWebRtcEndpointsConnected().remove(sessionId);
        }
    }

    public static String getEnv(String key, String def) {
        String value = getEnv(key);
        return value == null ? def : value;
    }

    public static String getEnv(String key) {
        return System.getenv(key);
    }

    public static String generateUuid() {
        log.debug("Generating UUID");
        return UUID.randomUUID().toString();
    }
}
