import java.io.*;
import java.net.*;
import java.util.*;

public class HelloSender extends Thread implements Runnable{
    TreeMap<String,TableEntry> table;
    int helloInterval;

    public HelloSender (TreeMap<String,TableEntry> dadsTable, int helloInt){
        this.table = dadsTable;
        this.helloInterval = helloInt;
    }

    TreeMap<String,InetAddress> parseToSend(TreeMap<String, TableEntry> table){        // Cria um set com todos os targets a 1 de distância
        TreeMap<String,InetAddress> parsedTree = new TreeMap<>();
        for (Map.Entry<String,TableEntry>  entry: table.entrySet()){
            TableEntry aux = entry.getValue();
            if(aux.isTTL1()){
                parsedTree.put(entry.getKey(),entry.getValue().getTarget());
            }
        }
        return parsedTree;
    }

    public void run(){

        try {
            System.out.println(" Server is Running  ");

            while (true) {

                DatagramSocket ds = new DatagramSocket(9999);

                InetAddress localhost = InetAddress.getLocalHost();
                String localHostName = (localhost.getHostName()).trim();

                //Send, utiliza o datagram socket para enviar
                TreeMap<String,InetAddress> parsedKeyTree = parseToSend(table);  // Prepara o set com os nós a 1 de distancia para enviar por udp
                HelloPacket data = new HelloPacket(parsedKeyTree,localHostName);     // Cria um objeto com o set

                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                sendData.writeObject(data);                                            // Serializa o objeto para o poder enviar
                sendData.flush();                                                      //
                byte[] sendDataBytes = byteOut.toByteArray();                          //
                DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length);  // Prepara o pacote
                ds.send(sendPacket);   //Envia o pacote

                ds.close();

                Thread.sleep(helloInterval*1000); //Espera o tempo entre Hellos

            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
