import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.String;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class applayer_worker implements Runnable{
    private TreeMap<String,TableEntry> table; //tabela
    private Socket socket; //socket de onde recebe o pacote
    private String name; //nome do nodo
    //private applayer_packet packet2send = null;
    //private Inet6Address targetIP = null;

    public applayer_worker(String name, Socket socket, TreeMap<String,TableEntry> table){
        this.table = table;
        this.socket = socket;
        this.name = name;
    }

    public void run(){
        try {
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

            applayer_packet packet = (applayer_packet) is.readObject();//recebe o pacote
            socket.close();
            String target = packet.getTarget();

            if(this.name.equals(target)){//verifica se o pacote é para este nodo ou para reencaminhar
                if (packet instanceof applayer_packetPedido){                          //Se o pacote for do tipo pedido de noticia
                    applayer_packetPedido packetPedido = (applayer_packetPedido) packet;       //cast
                    String newTarget = packetPedido.getFrom();                                 //destino
                    System.out.println("Recebi um pedido de "+newTarget);
                    InetAddress localhost = InetAddress.getLocalHost();
                    String localHostName = (localhost.getHostName()).trim();
                    Path path = Paths.get("news"+localHostName+".txt");                                        //
                    byte[] data = Files.readAllBytes(path);                                    //lê as suas noticias
                    applayer_packetNoticia toSend = new applayer_packetNoticia(newTarget,null,data,localHostName);
                    Socket nextNode = new Socket(table.get(newTarget).getNextJump(),9999);
                    ObjectOutputStream nos = new ObjectOutputStream(nextNode.getOutputStream());
                    nos.writeObject(toSend);//envia pacote de noticias para o proximo nodo
                    nos.close();
                }
                else{                                                   //caso seja um pacote de noticias
                    applayer_packetNoticia packetNoticia = (applayer_packetNoticia) packet;                 //Guarda-as num .txt chamado "newsFrom<nodo de onde veio>.txt
                    String news = new String(packetNoticia.getNews(), "UTF-8");
                    String from = packetNoticia.getFrom();
                    PrintWriter out = new PrintWriter("newsFrom"+from+".txt");
                    out.print(news);
                    out.close();
                    System.out.println("News from "+from+" saved.\n");
                }
            }
            else{//Reencaminhar, conecta ao socket tcp do próximo node
                InetAddress nextJump = table.get(target).getNextJump();
                Socket nextNode = new Socket(nextJump, 9999);
                ObjectOutputStream nos = new ObjectOutputStream(nextNode.getOutputStream());
                nos.writeObject(packet);//envia pacote para o proximo nodo
                nos.close();
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
