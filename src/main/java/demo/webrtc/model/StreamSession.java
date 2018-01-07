package demo.webrtc.model;

import org.kurento.client.MediaPipeline;
import org.kurento.client.PlayerEndpoint;
import org.kurento.client.RecorderEndpoint;
import org.kurento.client.WebRtcEndpoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kantipud on 21-12-2017.
 */
public class StreamSession {

    private String streamId;
    private MediaPipeline mediaPipeline;
    private PlayerEndpoint player;
    private RecorderEndpoint recorderEndpoint;
    private String recordingPath;
    private Map<String, WebRtcEndpoint> webRtcEndpointsConnected = new HashMap<>();

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public MediaPipeline getMediaPipeline() {
        return mediaPipeline;
    }

    public void setMediaPipeline(MediaPipeline mediaPipeline) {
        this.mediaPipeline = mediaPipeline;
    }

    public PlayerEndpoint getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEndpoint player) {
        this.player = player;
    }

    public RecorderEndpoint getRecorderEndpoint() {
        return recorderEndpoint;
    }

    public void setRecorderEndpoint(RecorderEndpoint recorderEndpoint) {
        this.recorderEndpoint = recorderEndpoint;
    }

    public String getRecordingPath() {
        return recordingPath;
    }

    public void setRecordingPath(String recordingPath) {
        this.recordingPath = recordingPath;
    }

    public Map<String, WebRtcEndpoint> getWebRtcEndpointsConnected() {
        return webRtcEndpointsConnected;
    }
}
