import java.util.Set;

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
}
