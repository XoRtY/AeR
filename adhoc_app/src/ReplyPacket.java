import java.net.InetAddress;
import java.util.ArrayList;

public class ReplyPacket {
    String targetS;
    String originS;
    InetAddress origin;

    public ReplyPacket (String target, String origin){
        this.targetS = target;
        this.originS = origin;
        this.origin = null;
    }

    public void setOrigin(InetAddress origin) {
        this.origin = origin;
    }

    public InetAddress getOrigin() {
        return origin;
    }

    public String getOriginS() {
        return originS;
    }

    public String getTargetS() {
        return targetS;
    }
}
