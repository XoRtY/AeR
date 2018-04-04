import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.String;

public class applayer_packetPedido extends applayer_packet{
    private int pedido;
    private String from;

    public applayer_packetPedido(String target, Stack<String> journey, String from){
        this.target = target;
        this.journey = journey;
        this.pedido = 1;
        this.from = from;
    }

    public int getPedido() {
        return pedido;
    }

    public String getFrom() {
        return from;
    }
}
