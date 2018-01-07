package demo.webrtc.app;

import demo.webrtc.model.StreamSession;
import demo.webrtc.utils.Constants;
import demo.webrtc.utils.WebrtcUtils;
import demo.webrtc.utils.videofile.RecordedFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.kurento.client.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kantipud on 22-12-2017.
 */
public class StreamPipelineHandler {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();
    private static final String RECORDER_FILE_PATH = RecordedFileUtils.RECORD_FILE_PREFIX + "/mnt/av/<REPLACE_HERE>.mp4";
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss-SSS");

    public static String addStream(String mediaUrl, String deviceId, String missionId, String networkCache, String useEncodedMedia) {
        String streamId = WebrtcUtils.generateUuid();
        String replacePath = missionId + "/" + deviceId + "/" + missionId + "__" + deviceId + "__" + streamId + "__" + timeFormat.format(new Date());
        String fullFilePath = StringUtils.replace(RECORDER_FILE_PATH, "<REPLACE_HERE>", replacePath);
        try {
            // User session
            StreamSession streamSession = new StreamSession();
            KurentoClient kurentoClient = Constants.getKurentoClient();
            MediaPipeline pipeline = kurentoClient.createMediaPipeline();
            streamSession.setMediaPipeline(pipeline);
            PlayerEndpoint player;
            if ("true".equals(useEncodedMedia)) {
                player = new PlayerEndpoint.Builder(pipeline, mediaUrl)
                        .withNetworkCache(Integer.valueOf(networkCache)).useEncodedMedia().build();
            } else {
                player = new PlayerEndpoint.Builder(pipeline, mediaUrl)
                        .withNetworkCache(Integer.valueOf(networkCache)).build();
            }

            streamSession.setPlayer(player);

            player.addErrorListener(new EventListener<ErrorEvent>() {
                @Override
                public void onEvent(ErrorEvent event) {
                    log.debug("ErrorEvent: {}", event.getDescription());
                    RecordedFileUtils.removeFileIfInvalidLength(fullFilePath);
                }
            });
            player.addEndOfStreamListener(new EventListener<EndOfStreamEvent>() {
                @Override
                public void onEvent(EndOfStreamEvent event) {
                    log.debug("EndOfStreamEvent: {}", event.getTimestamp());
                    StreamSession session = Constants.removeStreamSession(streamId);
                    if (session != null) {
                        session.getMediaPipeline().release();
                    }
                    RecordedFileUtils.removeFileIfInvalidLength(fullFilePath);
                    String thumbnail = RecordedFileUtils.createThumbnailIfValidRecording(session.getRecordingPath());
                    log.debug("Thumbnail generated at End Of Stream as: " + thumbnail);
                    //Retry mechanism can be here
                }
            });
            player.play();

            MediaProfileSpecType profile = getMediaProfile();
            RecorderEndpoint recorder = new RecorderEndpoint.Builder(pipeline, fullFilePath).stopOnEndOfStream()
                    .withMediaProfile(profile).build();
            recorder.addRecordingListener(new EventListener<RecordingEvent>() {
                @Override
                public void onEvent(RecordingEvent event) {
                    log.debug("Recorder RecordingEvent: {}", event.getTimestamp());
                    Constants.addStreamSession(streamId, streamSession);
                    log.debug("Recording successfully for stream id: " + streamId);
                }
            });
            recorder.addStoppedListener(new EventListener<StoppedEvent>() {
                @Override
                public void onEvent(StoppedEvent event) {
                    log.debug("Recorder StoppedEvent: {}", event.getTimestamp());
                    StreamSession session = Constants.removeStreamSession(streamId);
                    if (session != null) {
                        session.getMediaPipeline().release();
                    }
                    recorder.stop();
                    recorder.release();
                    RecordedFileUtils.removeFileIfInvalidLength(session.getRecordingPath());
                    String thumbnail = RecordedFileUtils.createThumbnailIfValidRecording(session.getRecordingPath());
                    log.debug("Thumbnail generated for recording as: " + thumbnail);
                }
            });
            recorder.addPausedListener(new EventListener<PausedEvent>() {
                @Override
                public void onEvent(PausedEvent event) {
                    log.debug("Recorder PausedEvent: {}", event.getTimestamp());
                }
            });
            recorder.addErrorListener(new EventListener<ErrorEvent>() {
                @Override
                public void onEvent(ErrorEvent event) {
                    log.debug("Recorder ErrorEvent: {}", event.getTimestamp());
                    recorder.stop();
                    recorder.release();
                }
            });

            streamSession.setRecorderEndpoint(recorder);
            streamSession.setRecordingPath(fullFilePath);
            player.connect(recorder, MediaType.AUDIO);
            player.connect(recorder, MediaType.VIDEO);
            recorder.record();
        } catch (Throwable t) {
            log.error(t);
        }
        return "Success";
    }

    private static MediaProfileSpecType getMediaProfile() {
        return MediaProfileSpecType.MP4;
    }
}
