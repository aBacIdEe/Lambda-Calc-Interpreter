public class Node {
    
    Node left;
    Node right; // (this other)
    Node above;
    String value = "error";
    
    public Node(String value) { // free variables
        this.value = value;
    }

    public Node(Node input) { // functions
        this.left = input;
    }

    public Node(Node input, Node function) { // functions
        this.left = input;
        this.right = function;
    }

    public String toString() {
        if (left != null && right != null) {
            return "(" + left.toString() + " " + right.toString() + ")";
        } else if (left != null) {
            return left.toString();
        } else if (value != null) {
            return value;
        } else {
            return "null";
        }
    }


}
