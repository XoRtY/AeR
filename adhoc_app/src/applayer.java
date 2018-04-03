import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.String;


public class applayer implements Runnable{
    private TreeMap<String,TableEntry> table;
    private String name = "";

    public applayer(TreeMap<String,TableEntry> table){
        this.table = table;

    }

    //Nodo aplicacional, fica a ouvir num socket TCP na porta 9999
    public void run() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            this.name = (localhost.getHostName()).trim();
            ServerSocket ss = new ServerSocket(9999);
            while(true){
                Socket socket = ss.accept();
                (new Thread(new applayer_worker(name, socket, table))).start();//Worker thread, para processar o packet
            }                                                                  //que chegou
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
