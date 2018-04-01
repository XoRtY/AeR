import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class HelloSender extends Thread implements Runnable{
    TreeMap<InetAddress,InetAddress> table = new TreeMap<>();

    public HelloSender (TreeMap<InetAddress,InetAddress> dadsTable){
        this.table = dadsTable;
    }

    Set<InetAddress> parseToSend(TreeMap<InetAddress, InetAddress> table){        // Cria um set com todos os targets a 1 de distância
        TreeMap<InetAddress, InetAddress> parsed = new TreeMap<>();
        for (Map.Entry<InetAddress,InetAddress>  entry: table.entrySet()){
            if(entry.getKey().equals(entry.getValue())){
                parsed.put(entry.getKey(),entry.getValue());
            }
        }
        return parsed.keySet();
    }

    public void run(){
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

                TreeMap<InetAddress,InetAddress> table = new TreeMap<>();
                Inet6Address IPAddress = (Inet6Address) Inet6Address.getByName("FF02::1");  //Ip do próprio

                //Send, utiliza o datagram socket para enviar
                Set<InetAddress> parsedKeySet = parseToSend(table);  // Prepara o set com os nós a 1 de distancia para enviar por udp
                HelloPacket data = new HelloPacket(parsedKeySet);     // Cria um objeto com o set

                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                sendData.writeObject(data);                                            // Serializa o objeto para o poder enviar
                sendData.flush();                                                      //
                byte[] sendDataBytes = byteOut.toByteArray();                          //
                DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, IPAddress, 9999);  // Prepara o pacote
                ds.send(sendPacket);   //Envia o pacote

                Thread.sleep(helloInterval*1000); //Espera o tempo entre Hellos

            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
