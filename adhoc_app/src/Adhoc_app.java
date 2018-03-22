import java.io.*;
import java.net.*;

public class Adhoc_app implements Runnable {

        public void run() {

            try {
                System.out.println(" Server is Running  ");
                ServerSocket ss = new ServerSocket(9999);
                DatagramSocket ds = new DatagramSocket(9999);


                while (true) {

                    Socket cs = ss.accept();
                    byte[] receiveData = new byte[1024];
                    byte[] sendData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    ds.receive(receivePacket);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(cs.getInputStream()));
                    PrintWriter writer= new PrintWriter(cs.getOutputStream());

                    String data1 = reader.readLine().trim();
                    String data2 = reader.readLine().trim();

                    int num1 = Integer.parseInt(data1);
                    int num2 = Integer.parseInt(data2);

                    int result = num1 + num2;


                    cs.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }

    public static void main(String argv[]) throws Exception {
        (new Thread(new Adhoc_app())).start();
    }

}