package demo.webrtc.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import demo.webrtc.model.StreamSession;
import demo.webrtc.utils.Constants;
import demo.webrtc.utils.WebrtcUtils;
import org.apache.logging.log4j.LogManager;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * Created by kantipud on 21-12-2017.
 */
public class WebrtcChannelHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();

    @Autowired
    private KurentoClient kurento;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

        log.debug("Incoming message: {}", jsonMessage);

        switch (jsonMessage.get("id").getAsString()) {
            case "start":
                start(session, jsonMessage);
                break;
            case "stop": {
                StreamSession streamSession = Constants.getStreamSession(jsonMessage.get("streamId").getAsString());
                WebRtcEndpoint webRtcEndpoint = streamSession.getWebRtcEndpointsConnected().get(session.getId());
                if (webRtcEndpoint != null) {
                    streamSession.getWebRtcEndpointsConnected().remove(session.getId());
                }
                break;
            }
            case "onIceCandidate": {
                JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();
                StreamSession streamSession = Constants.getStreamSession(jsonMessage.get("streamId").getAsString());
                WebRtcEndpoint webRtcEndpoint = streamSession.getWebRtcEndpointsConnected().get(session.getId());
                if (webRtcEndpoint != null) {
                    IceCandidate candidate = new IceCandidate(jsonCandidate.get("candidate").getAsString(),
                            jsonCandidate.get("sdpMid").getAsString(),
                            jsonCandidate.get("sdpMLineIndex").getAsInt());
                    webRtcEndpoint.addIceCandidate(candidate);
                }
                break;
            }
            default:
                sendErrorForSession(session, "Invalid message with id " + jsonMessage.get("id").getAsString());
                break;
        }
    }

    private void start(final WebSocketSession session, JsonObject jsonMessage) {
        try {
            // User session
            StreamSession streamSession = Constants.getStreamSession(jsonMessage.get("streamId").getAsString());
            if (streamSession == null) {
                sendErrorForSession(session, "No valid stream found for streamId: " + jsonMessage.get("streamId").getAsString());
                return;
            }
            MediaPipeline pipeline = streamSession.getMediaPipeline();
            WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).useDataChannels()
                    .build();
            streamSession.getWebRtcEndpointsConnected().put(session.getId(), webRtcEndpoint);

            // ICE candidates
            webRtcEndpoint.addOnIceCandidateListener(new EventListener<OnIceCandidateEvent>() {
                @Override
                public void onEvent(OnIceCandidateEvent event) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", "iceCandidate");
                    response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                    try {
                        synchronized (session) {
                            session.sendMessage(new TextMessage(response.toString()));
                        }
                    } catch (IOException e) {
                        log.debug(e.getMessage());
                    }
                }
            });

            // Media logic
            streamSession.getPlayer().connect(webRtcEndpoint);

            // SDP negotiation (offer and answer)
            String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
            String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);

            JsonObject response = new JsonObject();
            response.addProperty("id", "startResponse");
            response.addProperty("sdpAnswer", sdpAnswer);
            response.addProperty("recordPath", streamSession.getRecordingPath());

            synchronized (session) {
                session.sendMessage(new TextMessage(response.toString()));
            }

            webRtcEndpoint.gatherCandidates();

        } catch (Throwable t) {
            sendErrorForSession(session, t.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.debug("Socket Session closed for session id: " + session.getId() + " with status: " + status.getReason());
        WebrtcUtils.removeMatchingSocketSessionId(session.getId());
    }

    private void sendErrorForSession(WebSocketSession session, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "error");
        response.addProperty("message", message);
        sendMessage(session, response.toString());
    }

    private synchronized void sendMessage(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.error("Exception sending message", e);
        }
    }
}
