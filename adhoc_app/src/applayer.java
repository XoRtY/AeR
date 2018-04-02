import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Scanner;


public class applayer implements Runnable{
    private TreeMap<InetAddress,InetAddress> table;

    public applayer(TreeMap<InetAddress,InetAddress> table){
        this.table = table;
    }

    public void run() {

    }
}
