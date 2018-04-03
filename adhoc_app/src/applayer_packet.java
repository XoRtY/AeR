import java.util.Stack;
import java.lang.String;

public abstract class applayer_packet {
    String target;
    Stack<String> journey; //stack nodos de passagem

    public String popNode(){
        return journey.pop();
    }

    public String getTarget() {
        return this.target;
    }

    public Stack<String> getJourney(){
        return this.journey;
    }
}
