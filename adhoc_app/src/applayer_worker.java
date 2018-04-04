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

            if(this.name.compareTo(target) == 0){//verifica se o pacote é para este nodo ou para reencaminhar
                if (packet instanceof applayer_packetPedido){
                    applayer_packetPedido packetPedido = (applayer_packetPedido) packet;
                    String newTarget = packetPedido.getFrom();
                    Path path = Paths.get("/news.txt");
                    byte[] data = Files.readAllBytes(path);
                    InetAddress localhost = InetAddress.getLocalHost();
                    String localHostName = (localhost.getHostName()).trim();
                    applayer_packetNoticia toSend = new applayer_packetNoticia(newTarget,null,data,localHostName);
                    Socket nextNode = new Socket(table.get(newTarget).getNextJump(),9999);
                    ObjectOutputStream nos = new ObjectOutputStream(nextNode.getOutputStream());
                    nos.writeObject(packet);//envia pacote para o proximo nodo
                    nos.close();
                }
                else{
                    applayer_packetNoticia packetNoticia = (applayer_packetNoticia) packet;
                    String news = new String(packetNoticia.getNews(), "UTF-8");
                    String from = packetNoticia.getFrom();
                    PrintWriter out = new PrintWriter("newsFrom"+from+".txt");
                    out.print(news);
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
