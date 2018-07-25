package demo.webrtc.utils;

import org.apache.logging.log4j.LogManager;
import org.kurento.client.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by kantipud on 18-07-2018.
 */
public class RtpEndpointCreator {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();

    private PlayerEndpoint playerEndpoint;
    private RtpEndpoint rtpEndpoint;
    private MediaPipeline mediaPipeline;

    public void createEndpoints(String rtsp_url, String clientHost, String useEncodedMedia) {
        KurentoClient kurentoClient = Constants.getKurentoClient();
        mediaPipeline = kurentoClient.createMediaPipeline();

        if("true".equalsIgnoreCase(useEncodedMedia)){
            playerEndpoint = new PlayerEndpoint.Builder(mediaPipeline, rtsp_url)
                    .withNetworkCache(Integer.valueOf(0))
                    .useEncodedMedia()
                    .build();
        }else {
            playerEndpoint = new PlayerEndpoint.Builder(mediaPipeline, rtsp_url)
                    //.withNetworkCache(Integer.valueOf(0))
                    //.useEncodedMedia()
                    .build();
        }

        playerEndpoint.play();
        playerEndpoint.addErrorListener(event -> release("Error"));
        playerEndpoint.addEndOfStreamListener(event -> release("EOS"));

        rtpEndpoint = new RtpEndpoint.Builder(mediaPipeline).build();

        String sdpInfo = SdpConstant.getSdpAnswer(clientHost);
        log.info("#############################################################################################");
        log.info("Adding sdp offer: \n " + sdpInfo);
        log.info("#############################################################################################");

        //String generatedOffer =  rtpEndpoint.generateOffer();
        //System.out.println("Generated sdp offer : \n " + generatedOffer);

        String generatedAnswer = rtpEndpoint.processOffer(sdpInfo);

        playerEndpoint.connect(rtpEndpoint, MediaType.VIDEO);

        log.info("#############################################################################################");
        log.info("Generated answer is : \n " + generatedAnswer);
        log.info("#############################################################################################");

        try (PrintWriter out = new PrintWriter("D:\\userdata\\kantipud\\Desktop\\play.sdp")) {
            out.println(generatedAnswer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        rtpEndpoint.addMediaFlowInStateChangeListener(event -> {
            log.info("#############################################################################################");
            log.info("Generated IN addMediaFlowInStateChangeListener as : \n " + event.getMediaType().name());
            log.info("#############################################################################################");
        });

        rtpEndpoint.addMediaFlowOutStateChangeListener(event -> {
            log.info("#############################################################################################");
            log.info("Generated OUT addMediaFlowOutStateChangeListener as : \n " + event.getMediaType().name());
            log.info("#############################################################################################");
        });

    }

    public void release(String eventName) {
        log.info("Releasing playerendpoint and rtpendpoint for event {} ", eventName);
        if (playerEndpoint != null) {
            playerEndpoint.release();
        }
        if (mediaPipeline != null) {
            mediaPipeline.release();
        }
        if (rtpEndpoint != null) {
            rtpEndpoint.release();
        }
    }
}
