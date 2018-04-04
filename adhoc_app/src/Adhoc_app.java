import java.lang.String;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.UUID;

public class Adhoc_app implements Runnable {

    TreeMap<String,TableEntry> table = new TreeMap<>();
    boolean waitingReply = false;

    public static String generateString() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

        public void run(){

            Scanner inVars = new Scanner(System.in);

            //Cria o worker que manda hello packets e corre o seu run()
            System.out.println("Hello Interval in seconds:"); //Tempo entre cada hello
            int helloInterval = inVars.nextInt();
            HelloSender senderWorker = new HelloSender(table, helloInterval);
            senderWorker.start();

            //Cria o worker que recebe packets e corre o seu run()
            System.out.println("Dead Interval in seconds:");  //tempo que fica a espera de um hello
            int deadInterval = inVars.nextInt();
            PacketReceiver receiverWorker = new PacketReceiver(table, waitingReply, deadInterval);
            receiverWorker.start();

            //Cria o worker que recebe packets tcp
            applayer_PakcetReceiver receiverWorkerTcp = new applayer_PakcetReceiver(table);
            receiverWorkerTcp.start();


            String inLoop = "y"; //variavel de verificação do loop
            InetAddress localhost = null;
            try {
                localhost = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            assert localhost != null;
            String localHostName = (localhost.getHostName()).trim();

            System.out.println("It's routing time...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Entra no loop de pedir noticias
            while(inLoop.equals("y")){
                System.out.println("News from:");
                String targetNews = inVars.next(); //Pede um target para pedir noticias
                if (table.containsKey(targetNews)){  //Se estiver na tabela de encaminhamento
                    applayer_packetPedido request = new applayer_packetPedido(targetNews,null,localHostName);  //Cria um packet de pedido de noticias
                    InetAddress nextJump = table.get(targetNews).getNextJump();  //pega no proximo salto
                    Socket nextNode = null;
                    try {
                        nextNode = new Socket(nextJump, 9999);                                         //prepara o socket para o proximo nodo   //AQUI NUNO
                        ObjectOutputStream nos = new ObjectOutputStream(nextNode.getOutputStream());         //serializa
                        nos.writeObject(request);                                                           //envia pacote para o proximo nodo
                        nos.close();                                                                         //fecha o socket
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{                                                                                       //Se o target não está na tabela
                    System.out.println("Target not in current table, insert radius for search:");           //Pergunta o maximo numero de saltos
                    int radius = inVars.nextInt();
                    System.out.println("Insert desired timeout time, in seconds:");                         //Pergunta o tempo de timeout
                    int timeout = inVars.nextInt();
                    RequestSender requestWorker = new RequestSender(targetNews, radius);                    //Cria um worker de routeRequest
                    requestWorker.run();                                                                    //Corre-o
                    int timeoutAux = 0;
                    waitingReply = true;
                    while(waitingReply && timeoutAux<timeout){                                               //Enquanto nao receber resposta ou passar o tempo
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        timeoutAux++;                                                                        //incrementa o tempo
                    }
                    requestWorker.interrupt();                                                               //para o request worker
                    if (timeoutAux<timeout){                                                                 //Se não passar o tempo de timeout
                        applayer_packetPedido request = new applayer_packetPedido(targetNews,null,localHostName);  //cria o pedido de noticias
                        InetAddress nextJump = table.get(targetNews).getNextJump();
                        Socket nextNode = null;
                        try {
                            nextNode = new Socket(nextJump, 9999);                                                      //AQUI NUNO
                            ObjectOutputStream nos = new ObjectOutputStream(nextNode.getOutputStream());
                            nos.writeObject(request);  //envia pacote de pedido de noticias para o proximo nodo
                            ObjectInputStream nis = new ObjectInputStream(nextNode.getInputStream());
                            Object o = nis.readObject();
                            applayer_packetNoticia noticia = (applayer_packetNoticia) o;
                            nos.close();               //fecha o socket
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println("Pretende continuar?(y for yes, n for no)");         //pede a variavel de continuação no loop
                inLoop = inVars.next();
            }
        }

    public static void main(String argv[]) throws Exception {
        InetAddress localhost = InetAddress.getLocalHost();
        String localHostName = (localhost.getHostName()).trim();
        String randomNews = generateString();
        PrintWriter out = new PrintWriter("news"+localHostName+".txt");
        out.print(randomNews);
        out.close();

        (new Thread(new Adhoc_app())).start();
    }

}