import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.String;

public class applayer_packetNoticia extends applayer_packet {
    private byte[] news;

    public applayer_packetNoticia(String target, Stack<String> journey, byte[] news){
        this.target = target;
        this.journey = journey;
        this.news = news;
    }

    public Stack<String> getJourney(){
        return this.journey;
    }
}
