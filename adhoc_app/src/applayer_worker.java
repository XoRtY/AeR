import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.String;

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
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

            applayer_packet packet = (applayer_packet) is.readObject();//recebe o pacote
            socket.close();
            String target = packet.getTarget();

            if(this.name.compareTo(target) == 0){//verifica se o pacote é para este nodo ou para reencaminhar

            }
            else{//Reencaminhar, conecta ao socket tcp do próximo nodo
                TableEntry mytable = table.get(name);
                String nextNodename = packet.popNode();//consulta o proximo nodo fazendo pop da stack contida no pacote
                InetAddress nextNodeAddress = mytable.getNextJump();//Retificar se correta esta linha
                Socket nextNode = new Socket(nextNodeAddress, 9990);
                ObjectOutputStream nos = new ObjectOutputStream(nextNode.getOutputStream());
                ObjectInputStream nis = new ObjectInputStream(nextNode.getInputStream());
                nos.writeObject(packet);//envia pacote para o proximo nodo
                nos.close();
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
