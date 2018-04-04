import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class ReplyPacket implements Serializable {
    String targetS;
    String originS;
    InetAddress origin;
    boolean inRadius;

    public ReplyPacket (String target, String origin){
        this.targetS = target;
        this.originS = origin;
        this.origin = null;
        this.inRadius = true;
    }

    public ReplyPacket(String target){
        this.targetS = target;
        this.inRadius = false;
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

    public boolean isInRadius() {
        return inRadius;
    }
}
