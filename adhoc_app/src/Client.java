import java.io.*;
import java.net.Socket;

public class Client {

    /*public static void main(String argv[])
    {
        try{
            Socket socketClient= new Socket("localhost",9999);
            System.out.println("Client: "+"Connection Established");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            PrintWriter writer= new PrintWriter(socketClient.getOutputStream());

            String msg;
            msg = reader.readLine();
            while (!msg.equals("bye")){
                System.out.println(msg);
                msg = reader.readLine();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }*/
}