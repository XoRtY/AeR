import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.Integer.parseInt;

public class Adhoc_app implements Runnable {

        Set<InetAddress> parseToSend(TreeMap<InetAddress, InetAddress> table){
            TreeMap<InetAddress, InetAddress> parsed = new TreeMap<>();
            for (Map.Entry<InetAddress,InetAddress>  entry: table.entrySet()){
                if(entry.getKey().equals(entry.getValue())){
                    parsed.put(entry.getKey(),entry.getValue());
                }
            }
            return parsed.keySet();
        }


        public void run() {

            try {
                System.out.println(" Server is Running  ");
                ServerSocket ss = new ServerSocket(9999);
                DatagramSocket ds = new DatagramSocket(9999);
                MulticastSocket ms = new MulticastSocket(9999);

                while (true) {

                    TreeMap<InetAddress,InetAddress> table = new TreeMap<>();

                    //Socket cs = ss.accept();
                    byte[] receiveData = new byte[1024];
                    //byte[] sendData = new byte[1024];

                    //Join multicast group
                    InetAddress group = InetAddress.getByName("FF02::1");
                    Inet6Address IPAddress = (Inet6Address) Inet6Address.getByName("FF02::1");
                    ms.joinGroup(group);

                    //Send, utiliza o datagram socket para enviar
                    //String tableString = table.toString();
                    //String toSend = "H1"+tableString;
                    //sendData = toSend.getBytes();
                    Set <InetAddress> parsedKeySet = parseToSend(table);
                    HelloPacket data = new HelloPacket(parsedKeySet);

                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    ObjectOutputStream sendData = new ObjectOutputStream(byteOut);
                    sendData.writeObject(data);
                    sendData.flush();
                    byte[] sendDataBytes = byteOut.toByteArray();
                    DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, IPAddress, 9999);
                    ds.send(sendPacket);

                    //Receive, utiliza o multicast socket para receber
                    DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                    ms.receive(receivedPacket);
                    ByteArrayInputStream bis = new ByteArrayInputStream(receiveData);
                    ObjectInput in = null;
                    in = new ObjectInputStream(bis);
                    Object o = in.readObject();

                    /*String msg = new String(receivedPacket.getData(), receivedPacket.getOffset(),
                            receivedPacket.getLength());
                    String type = msg.substring(0,1);
                    String ttlString = msg.substring(1,2);
                    String peerTableString = msg.substring(2);
                    int ttl = parseInt(ttlString);*/

                    if(o instanceof HelloPacket){
                        HelloPacket received = (HelloPacket) o;
                        InetAddress from = receivedPacket.getAddress();
                        if(table.containsKey(from) && !table.get(from).equals(from)){
                            table.replace(from,from);
                        }
                        if(!table.containsKey(from)){
                            table.put(from,from);
                        }
                        Set<InetAddress> peerKeySet = received.getPeers();
                        for(InetAddress entry : peerKeySet){
                            if(!table.containsKey(entry)){
                                table.put(entry,from);
                            }
                        }
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
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    public static void main(String argv[]) throws Exception {
        (new Thread(new Adhoc_app())).start();
    }

}