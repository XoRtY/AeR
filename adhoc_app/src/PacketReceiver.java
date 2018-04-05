import java.io.*;
import java.net.*;
import java.util.*;

public class PacketReceiver extends Thread implements Runnable {
    TreeMap<String, TableEntry> table = new TreeMap<>();
    boolean waitingReply;
    int deadInterval;

    public PacketReceiver(TreeMap<String, TableEntry> dadsTable, boolean waitingReply, int deadInt) {
        this.table = dadsTable;
        this.waitingReply = waitingReply;
        this.deadInterval = deadInt;
    }

    public void run() {

        try {
            System.out.println(" PacketReceiver is Running  ");

            while (true) {
                MulticastSocket ms = new MulticastSocket(9999);
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

                if (o instanceof HelloPacket) {                                    // Caso o objeto que vem no pacote seja um Hello packet
                    HelloPacket received = (HelloPacket) o;                      // Parse para hello packet
                    String peerName = received.getFromName();
                    InetAddress from = receivedPacket.getAddress();              // IP do transmitter
                    if (table.containsKey(peerName) && !table.get(peerName).getNextJump().equals(from)) {   //Caso já tenha este target na tabela de encaminhamento mas tiver mais de 1 de distancia
                        TableEntry aux = new TableEntry(from, from);
                        table.replace(peerName, aux);
                    }
                    if (!table.containsKey(peerName)) {                                   //Caso não tenha este target na tabela de encaminhamento
                        TableEntry aux = new TableEntry(from, from);
                        table.put(peerName, aux);
                    }
                    TreeMap<String, InetAddress> peerKeySet = received.getPeers(); //Pega no set com os targets e adiciona-os, pondo como prox salto o router que enviou o pacote

                    for (Map.Entry<String, InetAddress> entry : peerKeySet.entrySet()) {
                        if (!table.containsKey(entry.getKey())) {
                            TableEntry aux = new TableEntry(entry.getValue(), from);
                            table.put(entry.getKey(), aux);
                        }
                    }
                }

                if (o instanceof RequestPacket) {                              //Caso seja um pacote do tipo route request
                    RequestPacket received = (RequestPacket) o;
                    String originName = received.getOriginName();
                    InetAddress localhost = InetAddress.getLocalHost();
                    String localHostName = (localhost.getHostName()).trim();
                    String toName = received.getToName();
                    if (received.getRadius()>0){
                        if (received.getOrigin() == null){
                            received.setOrigin(receivedPacket.getAddress());
                        }
                        if (toName.equals(localHostName)){
                            if(!table.containsKey(originName)){
                                TableEntry aux = new TableEntry(received.getOrigin(),receivedPacket.getAddress());
                                table.put(originName,aux);
                            }
                            int radToReply = received.getRadiusO() - received.getRadius() + 1;
                            ReplyPacket reply = new ReplyPacket(originName, localHostName, radToReply);           //prepara um route reply
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                            ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                            sendData.writeObject(reply);                                             // Serializa o objeto para o poder enviar
                            sendData.flush();                                                      //
                            byte[] sendDataBytes = byteOut.toByteArray();
                            InetAddress target = InetAddress.getByName("FF02::1");
                            DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, target,9999);  // Prepara o pacote
                            DatagramSocket ds = new DatagramSocket();
                            ds.send(sendPacket);                                                    //envia o route reply para o proximo nodo
                            ds.close();
                        }
                        else {
                            if(!table.containsKey(originName)){
                                TableEntry aux = new TableEntry(received.getOrigin(),receivedPacket.getAddress());
                                table.put(originName,aux);
                            }
                            received.decRadius();                                                  // decrementa o radius de pesquisa
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                            ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                            sendData.writeObject(received);                                        // Serializa o objeto para o poder enviar
                            sendData.flush();                                                      //
                            byte[] sendDataBytes = byteOut.toByteArray();                          //
                            InetAddress target = InetAddress.getByName("FF02::1");
                            DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, target,9999);  // Prepara o pacote
                            DatagramSocket ds = new DatagramSocket();
                            ds.send(sendPacket);   //re-envia em multicast
                            ds.close();
                        }
                    }
                }

                if(o instanceof ReplyPacket){                                                   //caso o pacote seja do tipo route reply
                    InetAddress localhost = InetAddress.getLocalHost();
                    String localHostName = (localhost.getHostName()).trim();
                    ReplyPacket reply = (ReplyPacket) o;
                    String origin = reply.getOriginS();
                    if (reply.getRadius()>0) {
                        if (reply.getOrigin() == null) {
                            reply.setOrigin(receivedPacket.getAddress());
                        }
                        if (reply.getTargetS().equals(localHostName)) {                               //se for para o proprio
                            //if (reply.isInRadius()){                                                //verifica se o radius foi ultrapassado
                            if (!table.containsKey(origin)) {                                    //adiciona a origem a sua tabela de encaminhamento
                                TableEntry aux = new TableEntry(reply.getOrigin(), receivedPacket.getAddress());
                                table.put(reply.getOriginS(), aux);
                            }
                            waitingReply = false;                                               //avisa que já recebeu resposta
                            //}
                        /*else{
                            System.out.println("Radius limit reached");                         //avisa que o radius foi ultrapassado
                            waitingReply = false;                                               //avisa que recebeu resposta
                        }*/
                        } else {                                                                      //apenas re-encaminha e adiciona a origem a sua tabela de encaminhamento (caso nao a tenha)
                            reply.dcRadius();
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                            ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                            sendData.writeObject(reply);                                           // Serializa o objeto para o poder enviar
                            sendData.flush();                                                      //
                            byte[] sendDataBytes = byteOut.toByteArray();                          //
                            InetAddress target = InetAddress.getByName("FF02::1");
                            DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, target, 9999);  // Prepara o pacote  // Prepara o pacote
                            DatagramSocket ds = new DatagramSocket();
                            ds.send(sendPacket);
                            ds.close();
                            if (!table.containsKey(origin)) {
                                TableEntry aux = new TableEntry(reply.getOrigin(), receivedPacket.getAddress());
                                table.put(reply.getOriginS(), aux);
                            }
                        }
                    }
                }
                ms.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
