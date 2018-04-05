import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class ReplyPacket implements Serializable {
    String targetS;
    String originS;
    InetAddress origin;
    int radius;
    boolean inRadius;

    public ReplyPacket (String target, String origin, int rad){
        this.targetS = target;
        this.originS = origin;
        this.origin = null;
        this.inRadius = true;
        this.radius = rad;
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

    public int getRadius() {
        return radius;
    }

    public void dcRadius(){
        this.radius--;
    }
}
