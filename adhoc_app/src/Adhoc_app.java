import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class Adhoc_app implements Runnable {

        Set<InetAddress> parseToSend(TreeMap<InetAddress, InetAddress> table){        // Cria um set com todos os targets a 1 de distância
            TreeMap<InetAddress, InetAddress> parsed = new TreeMap<>();
            for (Map.Entry<InetAddress,InetAddress>  entry: table.entrySet()){
                if(entry.getKey().equals(entry.getValue())){
                    parsed.put(entry.getKey(),entry.getValue());
                }
            }
            return parsed.keySet();
        }


        public void run() {

            Scanner inVars = new Scanner(System.in);
            System.out.println("Hello Interval in seconds: "); //Tempo entre cada hello
            int helloInterval = inVars.nextInt();
            System.out.println("Dead Interval in seconds: ");  //tempo que fica a espera de um hello
            int deadInterval = inVars.nextInt();

            try {
                System.out.println(" Server is Running  ");
                ServerSocket ss = new ServerSocket(9999);
                DatagramSocket ds = new DatagramSocket(9999);
                MulticastSocket ms = new MulticastSocket(9999);

                while (true) {

                    Thread.sleep(helloInterval*1000); //Espera o tempo entre Hellos

                    TreeMap<InetAddress,InetAddress> table = new TreeMap<>();

                    //Socket cs = ss.accept();
                    byte[] receiveData = new byte[1024];
                    //byte[] sendData = new byte[1024];

                    //Join multicast group
                    InetAddress group = InetAddress.getByName("FF02::1"); //Ip do grupo multicast
                    Inet6Address IPAddress = (Inet6Address) Inet6Address.getByName("FF02::1");  //Ip do próprio
                    ms.joinGroup(group);

                    //Send, utiliza o datagram socket para enviar
                    //String tableString = table.toString();
                    //String toSend = "H1"+tableString;
                    //sendData = toSend.getBytes();
                    Set <InetAddress> parsedKeySet = parseToSend(table);  // Prepara o set com os nós a 1 de distancia para enviar por udp
                    HelloPacket data = new HelloPacket(parsedKeySet);     // Cria um objeto com o set

                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                    ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                    sendData.writeObject(data);                                            // Serializa o objeto para o poder enviar
                    sendData.flush();                                                      //
                    byte[] sendDataBytes = byteOut.toByteArray();                          //
                    DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, IPAddress, 9999);  // Prepara o pacote
                    ds.send(sendPacket);   //Envia o pacote

                    //Receive, utiliza o multicast socket para receber
                    DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);  // Prepara um objeto para receber o pacote
                    ms.receive(receivedPacket);  // Recebe o pacote
                    ByteArrayInputStream bis = new ByteArrayInputStream(receiveData); //
                    ObjectInput in = null;                                            // De-serializa o objeto que vem no pacote
                    in = new ObjectInputStream(bis);                                  //
                    Object o = in.readObject();                                       //

                    /*String msg = new String(receivedPacket.getData(), receivedPacket.getOffset(),
                            receivedPacket.getLength());
                    String type = msg.substring(0,1);
                    String ttlString = msg.substring(1,2);
                    String peerTableString = msg.substring(2);
                    int ttl = parseInt(ttlString);*/

                    if(o instanceof HelloPacket){                                    // Caso o objeto que vem no pacote seja um Hello packet
                        HelloPacket received = (HelloPacket) o;                      // Parse para hello packet
                        InetAddress from = receivedPacket.getAddress();              // IP do transmitter
                        if(table.containsKey(from) && !table.get(from).equals(from)){   //Caso já tenha este target na tabela de encaminhamento mas tiver mais de 1 de distancia
                            table.replace(from,from);
                        }
                        if(!table.containsKey(from)){                                   //Caso não tenha este target na tabela de encaminhamento
                            table.put(from,from);
                        }
                        Set<InetAddress> peerKeySet = received.getPeers();              //Pega no set com os targets e adiciona-os, pondo como prox salto o router que enviou o pacote
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    public static void main(String argv[]) throws Exception {
        (new Thread(new Adhoc_app())).start();
    }

}