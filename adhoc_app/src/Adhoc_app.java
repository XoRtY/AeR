import java.io.*;
import java.net.*;
import java.util.TreeMap;
import java.lang.String;

import static java.lang.Integer.parseInt;

public class Adhoc_app implements Runnable {

        public void run() {

            try {
                System.out.println(" Server is Running  ");
                ServerSocket ss = new ServerSocket(9999);
                DatagramSocket ds = new DatagramSocket(9999);
                MulticastSocket ms = new MulticastSocket(9999);

                while (true) {

                    TreeMap <Inet6Address, Inet6Address> table= new TreeMap <Inet6Address,Inet6Address>();

                    //Socket cs = ss.accept();
                    byte[] receiveData = new byte[1024];
                    byte[] sendData = new byte[1024];
                    InetAddress IPAddress = InetAddress.getByName("localhost");

                    //Join multicast group
                    InetAddress group = InetAddress.getByName("FF02::1");
                    ms.joinGroup(group);

                    //Send, utiliza o datagram socket para enviar
                    String toSend = "H1";
                    sendData = toSend.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9999);
                    ds.send(sendPacket);

                    //Receive, utiliza o multicast socket para receber
                    DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                    ms.receive(receivedPacket);
                    String msg = new String(receivedPacket.getData(), receivedPacket.getOffset(),
                            receivedPacket.getLength());
                    String type = msg.substring(0,1);
                    String ttlString = msg.substring(1,2);
                    int ttl = parseInt(ttlString);

                    if(type.equals("H")){
                        InetAddress from = receivedPacket.getAddress();
                    }



                    /*BufferedReader reader = new BufferedReader(new InputStreamReader(cs.getInputStream()));
                    PrintWriter writer= new PrintWriter(cs.getOutputStream());

                    String data1 = reader.readLine().trim();
                    String data2 = reader.readLine().trim();

                    int num1 = Integer.parseInt(data1);     Não percebo o objetivo deste pedaço de código - Matias
                    int num2 = Integer.parseInt(data2);     Já entendi, é só para a parte da troca de dados, não precisamos para já

                    int result = num1 + num2;*/


                    //cs.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }

    public static void main(String argv[]) throws Exception {
        (new Thread(new Adhoc_app())).start();
    }

}