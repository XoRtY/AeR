import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class PacketReceiver extends Thread implements Runnable{
    TreeMap<InetAddress,InetAddress> table = new TreeMap<>();

    public PacketReceiver(TreeMap<InetAddress,InetAddress> dadsTable){
        this.table = dadsTable;
    }

    public void run(){

        Scanner inVars = new Scanner(System.in);
        System.out.println("Dead Interval in seconds: ");  //tempo que fica a espera de um hello
        int deadInterval = inVars.nextInt();

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
                InetAddress group = InetAddress.getByName("FF02::1"); //Ip do grupo multicast
                Inet6Address IPAddress = (Inet6Address) Inet6Address.getByName("FF02::1");  //Ip do próprio
                ms.joinGroup(group);

                //Receive, utiliza o multicast socket para receber
                DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);  // Prepara um objeto para receber o pacote
                ms.receive(receivedPacket);  // Recebe o pacote
                ByteArrayInputStream bis = new ByteArrayInputStream(receiveData); //
                ObjectInput in = null;                                            // De-serializa o objeto que vem no pacote
                in = new ObjectInputStream(bis);                                  //
                Object o = in.readObject();                                       //

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

            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
