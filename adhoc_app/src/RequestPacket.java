import java.io.Serializable;
import java.net.InetAddress;
import java.util.Set;
import java.util.TreeMap;

public class RequestPacket implements Serializable {
    String toName;
    Set<String> visitedNodes;
    InetAddress origin;
    String originName;
    int radius;
    int radiusO;

    RequestPacket(String tName, Set<String> vNodes, String sender, int rad){
        this.toName = tName;
        this.visitedNodes = vNodes;
        this.originName = sender;
        this.origin = null;
        this.radius = rad;
        this.radiusO = rad;
    }

    public String getToName() {
        return toName;
    }

    public Set<String> getVisitedNodes() {
        return visitedNodes;
    }

    public void addVisitedNode(String node){
        this.visitedNodes.add(node);
    }

    public void setOrigin(InetAddress origin) {
        this.origin = origin;
    }

    public InetAddress getOrigin() {
        return origin;
    }

    public String getOriginName() {
        return originName;
    }

    public int getRadius() {
        return radius;
    }

    public void decRadius(){
        this.radius--;
    }

    public int getRadiusO() {
        return radiusO;
    }
}
