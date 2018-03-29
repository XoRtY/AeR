import java.net.InetAddress;
import java.util.Set;

public class HelloPacket {
    Set<InetAddress> directPeers;

    HelloPacket(Set<InetAddress> dirPeers){
        this.directPeers= dirPeers;
    }

    Set<InetAddress> getPeers(){
        return this.directPeers;
    }
}
