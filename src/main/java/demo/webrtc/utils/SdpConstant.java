package demo.webrtc.utils;

/**
 * Created by kantipud on 14-05-2018.
 */
public class SdpConstant {

    public static String getSdpAnswer(String clientHost) {
        String sdp = "v=0\r\n" + "o=- 12345 12345 IN IP4 " + clientHost + "\r\n" + "s=-\r\n"
                + "c=IN IP4 " + clientHost + "\r\n" + "t=0 0\r\n" + "m=video 52126 RTP/AVP 96 97 98\r\n"
                + "a=rtpmap:96 H264/90000\r\n" + "a=rtpmap:97 MP4V-ES/90000\r\n"
                + "a=rtpmap:98 H263-1998/90000\r\n" + "a=recvonly\r\n" + "b=AS:384\r\n";
        return sdp;
    }
}
