public class Node {
    
    Node function;
    Node input; // (this other)
    Node above;
    String value;
    
    public Node() {
    }

    public Node(Node input, Node function) {
        this.function = input;
        this.input = function;
    }


}
