import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.String;

public class applayer_packetPedido extends applayer_packet{
    private int pedido;

    public applayer_packetPedido(String target, Stack<String> journey){
        this.target = target;
        this.journey = journey;
        this.pedido = 1;
    }

    public int getPedido() {
        return pedido;
    }
}
