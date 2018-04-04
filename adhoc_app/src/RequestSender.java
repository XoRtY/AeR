//import sun.tools.jconsole.Tab;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

public class RequestSender extends Thread implements Runnable{
    String target;
    Set<String> visited;
    int radius;

    public RequestSender (String target, int rad){
        this.target = target;
        this.radius = rad;
    }

    public void run() {
        Scanner vars = new Scanner(System.in);

        try {
            System.out.println(" Server is Running  ");
            DatagramSocket ds = new DatagramSocket(9999);

                InetAddress localhost = InetAddress.getLocalHost();
                String localHostName = (localhost.getHostName()).trim();
                RequestPacket req = new RequestPacket(target, visited, localHostName, radius);

                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                sendData.writeObject(req);                                             // Serializa o objeto para o poder enviar
                sendData.flush();                                                      //
                byte[] sendDataBytes = byteOut.toByteArray();                          //
                DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length);  // Prepara o pacote
                ds.send(sendPacket);                                                   //envia pedido de route para o proximo nodo
                ds.close();

                return;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
