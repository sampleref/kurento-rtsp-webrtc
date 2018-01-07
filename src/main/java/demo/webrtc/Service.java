package demo.webrtc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import demo.webrtc.app.StreamPipelineHandler;
import demo.webrtc.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Spark;

import java.io.IOException;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.post;

public final class Service {

    private static final Logger logger = LogManager.getLogger();

    public Service() throws IOException {
        logger.info("Initializing the service...");
        try {
            startHttpVideoInfoListener();
        } catch (Exception e) {
            logger.error("While initializing the service " + e.getMessage());
            logger.debug(e);
            throw e;
        }
    }

    public static void startHttpVideoInfoListener() {
        int httpPort = Integer.parseInt(Constants.DEFUALT_HTTP_PORT);
        Spark.port(httpPort);
        logger.debug("HTTP listening on port " + httpPort);

        post("/updatemediainfo", (req, res) -> {
            ObjectMapper mapper = new ObjectMapper();
            final ObjectNode node = mapper.readValue(req.body(), ObjectNode.class);
            logger.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
            logger.debug("Rtsp Url is: " + node.get(Constants.RTSP_URL));
            logger.debug("Mission Id is: " + node.get(Constants.MISSION_ID_JSONKEY));
            logger.debug("Device Id is: " + node.get(Constants.DEVICE_ID_JSONKEY));
            logger.debug("Network Cache is: " + node.get(Constants.NETWORK_CACHE));
            logger.debug("Use Encoded Media is: " + node.get(Constants.USE_ENCODED_MEDIA));
            try {
                StreamPipelineHandler.addStream(node.get(Constants.RTSP_URL).asText()
                        , node.get(Constants.DEVICE_ID_JSONKEY).asText(), node.get(Constants.MISSION_ID_JSONKEY).asText()
                        , node.get(Constants.NETWORK_CACHE).asText()
                        , node.get(Constants.USE_ENCODED_MEDIA).asText());
                return "success";
            } catch (Exception e) {
                logger.error(e);
                logger.error("Exception raised in POST method /updatemediainfo : " + e.getMessage());
                return "failure";
            }
        });

        get("/streamsonline", (req, res) -> {
            String streamsIds = Constants.getStreamSessionMap().keySet().stream()
                    .collect(Collectors.joining(", "));
            return streamsIds;
        });
    }

}
