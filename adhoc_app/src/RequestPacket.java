import java.util.Set;
import java.util.TreeMap;

public class RequestPacket {
    String toName;
    Set<String> visitedNodes;

    RequestPacket(String tName, Set<String> vNodes){
        this.toName = tName;
        this.visitedNodes = vNodes;
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
}
