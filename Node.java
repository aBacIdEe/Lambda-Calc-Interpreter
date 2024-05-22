import java.util.HashSet;
import java.util.Set;

public class Node {
    
    Node left;
    Node right; // (this other)
    Node above;
    String value = "error";
    Set<String> var_names = new HashSet<String>();

    public static Set<String> getVarNames(Node home) {
        if (home.left != null) {
            home.var_names.addAll(getVarNames(home.left));
        }
        if (home.right != null) {
            home.var_names.addAll(getVarNames(home.right));
        }
        if (home.left == null && home.right == null) {
            if (home.value.charAt(0) == '\\') {
                home.var_names.add(home.value.substring(1, home.value.length() - 1));
            } else {
                home.var_names.add(home.value);
            }
        }
        return home.var_names;
    }

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
            if (left.toString().charAt(0) == '\\') {
                return "(" + left.toString() + "" + right.toString() + ")";
            }
            else {
                return "(" + left.toString() + " " + right.toString() + ")";
            }
        } else if (left != null) {
            return left.toString();
        } else if (value != null) {
            return value;
        } else {
            return "null";
        }
    }


}
