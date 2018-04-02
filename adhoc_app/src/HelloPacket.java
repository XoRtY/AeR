import java.net.InetAddress;
import java.util.TreeMap;

public class HelloPacket {
    TreeMap<String,InetAddress> directPeers;
    String fromName;

    HelloPacket(TreeMap<String,InetAddress> dirPeers, String hName){
        this.directPeers = dirPeers;
        this.fromName = hName;
    }

    public TreeMap<String,InetAddress> getPeers(){
        return this.directPeers;
    }

    public String getFromName() {
        return fromName;
    }
}
