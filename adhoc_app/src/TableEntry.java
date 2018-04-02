import java.net.InetAddress;

public class TableEntry {
    InetAddress target;
    InetAddress nextJump;

    public TableEntry(InetAddress t, InetAddress n){
        this.target = t;
        this.nextJump = n;
    }

    public boolean isTTL1(){
        boolean r = false;
        if (target.equals(nextJump)) r = true;
        return r;
    }

    public InetAddress getNextJump() {
        return nextJump;
    }

    public InetAddress getTarget() {
        return target;
    }
}
