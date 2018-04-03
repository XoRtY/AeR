import java.lang.String;
import java.io.*;
import java.net.*;
import java.util.*;

public class Adhoc_app implements Runnable {

    TreeMap<String,TableEntry> table = new TreeMap<>();

        public void run() {

            //Cria o worker que recebe packets e corre o seu run()
            PacketReceiver receiverWorker = new PacketReceiver(table);
            receiverWorker.run();

            //Cria o worker que manda hello packets e corre o seu run()
            HelloSender senderWorker = new HelloSender(table);
            senderWorker.run();
        }

    public static void main(String argv[]) throws Exception {
        (new Thread(new Adhoc_app())).start();
    }

}