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
            MulticastSocket ms = new MulticastSocket(9999);

            while (true) {

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
                    if (!table.containsKey(from)) {                                   //Caso não tenha este target na tabela de encaminhamento
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
                    String origin = received.getOriginName();
                    InetAddress localhost = InetAddress.getLocalHost();
                    String localHostName = (localhost.getHostName()).trim();
                    if(received.getRadius()>0) {                               //Se ainda tiver dentro do radius de procura
                        if(localHostName.equals(received.getToName())){        //Se for o nodo que está a ser procurado
                            if (!table.containsKey(origin)) {
                                TableEntry aux = new TableEntry(received.getOrigin(), receivedPacket.getAddress());     //adiciona a origem à sua tabela de encaminhamento
                                table.put(received.getOriginName(), aux);
                            }
                            ReplyPacket reply = new ReplyPacket(received.getOriginName(), localHostName);           //prepara um route reply
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                            ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                            sendData.writeObject(reply);                                             // Serializa o objeto para o poder enviar
                            sendData.flush();                                                      //
                            byte[] sendDataBytes = byteOut.toByteArray();
                            DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, table.get(origin).getNextJump(), 9999);
                            DatagramSocket ds = new DatagramSocket(9999);
                            ds.send(sendPacket);                                                    //envia o route reply para o proximo nodo
                            ds.close();
                        }
                        if (!table.containsKey(received.getToName())) {                             //se nao tiver na sua tabela de encaminhamendo
                            received.addVisitedNode(localHostName);
                            if (origin == null) {                                                   //caso tenha sido o primeiro salto, define o ip de origem
                                received.setOrigin(receivedPacket.getAddress());
                            } else {
                                if (!table.containsKey(origin)) {                                   //caso contrario adiciona o node a sua tabela de encaminhamento (se ainda nao o tiver)
                                    TableEntry aux = new TableEntry(received.getOrigin(), receivedPacket.getAddress());
                                    table.put(received.getOriginName(), aux);
                                }
                            }
                            received.decRadius();                                                  // decrementa o radius de pesquisa
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                            ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                            sendData.writeObject(received);                                        // Serializa o objeto para o poder enviar
                            sendData.flush();                                                      //
                            byte[] sendDataBytes = byteOut.toByteArray();                          //
                            DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length);  // Prepara o pacote
                            DatagramSocket ds = new DatagramSocket(9999);
                            ds.send(sendPacket);   //re-envia em multicast
                            ds.close();
                        } else {                                            //caso o radius tenha sido ultrapassado
                            if (!table.containsKey(origin)) {               //adiciona a origem a sua tabela de encaminhamento (caso nao a tenha)
                                TableEntry aux = new TableEntry(received.getOrigin(), receivedPacket.getAddress());
                                table.put(received.getOriginName(), aux);
                            }
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                            ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                            sendData.writeObject(received);                                             // Serializa o objeto para o poder enviar
                            sendData.flush();                                                      //
                            byte[] sendDataBytes = byteOut.toByteArray();                          //
                            DatagramPacket sendPacket = new DatagramPacket(sendDataBytes,
                                                                           sendDataBytes.length,
                                                                           table.get(received.getToName()).getNextJump(),
                                                                           9999);  // Prepara o pacote

                            DatagramSocket ds = new DatagramSocket(9999);
                            ds.send(sendPacket);                                                   //manda um route reply a dizer que o limite de radius foi atingido
                            ds.close();
                        }
                    }
                    else{
                        ReplyPacket reply = new ReplyPacket(received.getOriginName());
                        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                        ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                        sendData.writeObject(reply);                                           // Serializa o objeto para o poder enviar
                        sendData.flush();                                                      //
                        byte[] sendDataBytes = byteOut.toByteArray();                          //
                        DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, table.get(origin).getNextJump(), 9999);  // Prepara o pacote
                        DatagramSocket ds = new DatagramSocket(9999);
                        ds.send(sendPacket);
                        ds.close();
                    }
                }

                if(o instanceof ReplyPacket){                                                   //caso o pacote seja do tipo route reply
                    InetAddress localhost = InetAddress.getLocalHost();
                    String localHostName = (localhost.getHostName()).trim();
                    ReplyPacket reply = (ReplyPacket) o;
                    String origin = reply.getOriginS();
                    if(reply.getTargetS().equals(localHostName)){                               //se for para o proprio
                        if (reply.isInRadius()){                                                //verifica se o radius foi ultrapassado
                            if(!table.containsKey(origin)) {                                    //adiciona a origem a sua tabela de encaminhamento
                                TableEntry aux = new TableEntry(reply.getOrigin(), receivedPacket.getAddress());
                                table.put(reply.getOriginS(), aux);
                            }
                            waitingReply = false;                                               //avisa que já recebeu resposta
                        }
                        else{
                            System.out.println("Radius limit reached");                         //avisa que o radius foi ultrapassado
                            waitingReply = false;                                               //avisa que recebeu resposta
                        }
                    }
                    else{                                                                      //apenas re-encaminha e adiciona a origem a sua tabela de encaminhamento (caso nao a tenha)
                        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();           //
                        ObjectOutputStream sendData = new ObjectOutputStream(byteOut);         //
                        sendData.writeObject(reply);                                           // Serializa o objeto para o poder enviar
                        sendData.flush();                                                      //
                        byte[] sendDataBytes = byteOut.toByteArray();                          //
                        DatagramPacket sendPacket = new DatagramPacket(sendDataBytes, sendDataBytes.length, table.get(origin).getNextJump(), 9999);  // Prepara o pacote
                        DatagramSocket ds = new DatagramSocket(9999);
                        ds.send(sendPacket);
                        ds.close();
                        if(!table.containsKey(origin)) {
                            TableEntry aux = new TableEntry(reply.getOrigin(), receivedPacket.getAddress());
                            table.put(reply.getOriginS(), aux);
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
