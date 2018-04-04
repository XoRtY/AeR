//import sun.tools.jconsole.Tab;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

public class RequestSender extends Thread implements Runnable{
    String nodeName;
    Set<String> visited;
    TreeMap<String,TableEntry> table = new TreeMap<>();
    boolean waitingReply;

    public RequestSender (TreeMap<String, TableEntry> dTable, boolean waitingReply){
        this.table = dTable;
        this.waitingReply = waitingReply;
    }

    public void run() {
        Scanner vars = new Scanner(System.in);
        System.out.println("Timeout in seconds: ");
        int timeout = vars.nextInt();
        System.out.println("Radius: ");
        int radius = vars.nextInt();

        try {
            System.out.println(" Server is Running  ");
            ServerSocket ss = new ServerSocket(9999);
            DatagramSocket ds = new DatagramSocket(9999);
            MulticastSocket ms = new MulticastSocket(9999);



                InetAddress localhost = InetAddress.getLocalHost();
                String localHostName = (localhost.getHostName()).trim();
                RequestPacket req = new RequestPacket(nodeName, visited, localHostName, radius);

                /*Iterator it = table.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry =  (Map.Entry) it.next();   // No need, use containsKey
                    if(req.toName == entry.getKey()) r = true;
                }*/

                if(!table.containsKey(req.getToName())) {
                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                    ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                    sendData.writeObject(req);                                             // Serializa o objeto para o poder enviar
                    sendData.flush();                                                      //
                    byte[] sendDataBytes = byteOut.toByteArray();                          //
                    DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length);  // Prepara o pacote
                    ds.send(sendPacket);
                }

                int auxTimeout = 0;
                waitingReply = true;

                while (auxTimeout<timeout && waitingReply){
                    Thread.sleep(1000);
                    auxTimeout++;
                }
                waitingReply = false;
                return;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
