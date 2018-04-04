import java.lang.String;
import java.io.*;
import java.net.*;
import java.util.*;

public class Adhoc_app implements Runnable {

    TreeMap<String,TableEntry> table = new TreeMap<>();
    boolean waitingReply = false;

        public void run(){

            Scanner inVars = new Scanner(System.in);

            //Cria o worker que recebe packets e corre o seu run()
            System.out.println("Dead Interval in seconds: \n");  //tempo que fica a espera de um hello
            int deadInterval = inVars.nextInt();
            PacketReceiver receiverWorker = new PacketReceiver(table, waitingReply, deadInterval);
            receiverWorker.run();

            //Cria o worker que manda hello packets e corre o seu run()
            System.out.println("Hello Interval in seconds: \n"); //Tempo entre cada hello
            int helloInterval = inVars.nextInt();
            HelloSender senderWorker = new HelloSender(table, helloInterval);
            senderWorker.run();

            //Cria o worker que recebe packets tcp
            applayer_PakcetReceiver receiverWorkerTcp = new applayer_PakcetReceiver(table);
            receiverWorker.run();

            String inLoop = "y";
            InetAddress localhost = null;
            try {
                localhost = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            String localHostName = (localhost.getHostName()).trim();
            while(inLoop.equals("y")){
                System.out.println("News from:\n");
                String targetNews = inVars.next();
                if (table.containsKey(targetNews)){
                    applayer_packetPedido request = new applayer_packetPedido(targetNews,null,localHostName);
                    InetAddress nextJump = table.get(targetNews).getNextJump();
                    Socket nextNode = null;
                    try {
                        nextNode = new Socket(nextJump, 9999);
                        ObjectOutputStream nos = new ObjectOutputStream(nextNode.getOutputStream());
                        nos.writeObject(request);//envia pacote para o proximo nodo
                        nos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    System.out.println("Target not in current table, insert radius for search:");
                    int radius = inVars.nextInt();
                    System.out.println("Insert desired timeout time, in seconds:");
                    int timeout = inVars.nextInt();
                    RequestSender requestWorker = new RequestSender(targetNews, radius);
                    requestWorker.run();
                    int timeoutAux = 0;
                    waitingReply = true;
                    while(waitingReply | timeoutAux<timeout){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        timeoutAux++;
                    }
                    requestWorker.interrupt();
                    if (timeoutAux<timeout){
                        applayer_packetPedido request = new applayer_packetPedido(targetNews,null,localHostName);
                        InetAddress nextJump = table.get(targetNews).getNextJump();
                        Socket nextNode = null;
                        try {
                            nextNode = new Socket(nextJump, 9999);
                            ObjectOutputStream nos = new ObjectOutputStream(nextNode.getOutputStream());
                            nos.writeObject(request);//envia pacote para o proximo nodo
                            nos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    public static void main(String argv[]) throws Exception {
        (new Thread(new Adhoc_app())).start();
    }

}