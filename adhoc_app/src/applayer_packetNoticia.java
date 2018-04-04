import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.String;

public class applayer_packetNoticia extends applayer_packet {
    private byte[] news;
    private String from;

    public applayer_packetNoticia(String target, Stack<String> journey, byte[] news, String from){
        this.target = target;
        this.journey = journey;
        this.news = news;
        this.from = from;
    }

    public Stack<String> getJourney(){
        return this.journey;
    }

    public String getFrom() {
        return from;
    }

    public byte[] getNews() {
        return news;
    }
}
