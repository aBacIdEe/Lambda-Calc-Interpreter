import java.util.HashSet;
import java.util.Set;

public class Node {
    
    Node left;
    Node right; // (this other)
    Node above;
    String value = "";
    Set<String> var_names = new HashSet<String>();
    Set<String> bound_vars = new HashSet<String>();

    public static void findBoundVars(Node home) {
        // home.bound_vars.clear();
        home.bound_vars.addAll(home.above.bound_vars);
        // if (home != home.above) { // inherit aboves
        //     for (String var: home.above.bound_vars) {
        //         home.bound_vars.add(var);
        //     }
        // }
        // if left is lambda, then bind the right and call the right
        // if left is not lambda, then call on both
        if (home.left != null && home.right != null) {
            if ((!home.left.value.equals("")) && (home.left.value.charAt(0) == '\\' || home.left.value.charAt(0) == 'λ')) {
                home.right.bound_vars.add(home.left.value.substring(1, home.left.value.length() - 1));
            } else {
                findBoundVars(home.left);
            }
            findBoundVars(home.right);
        } else if (home.left != null) {
            findBoundVars(home.left);
        } else if (home.right != null) {
            findBoundVars(home.right);
        }
    }

    public static Set<String> getVarNames(Node home) {
        if (home.left != null) {
            home.var_names.addAll(getVarNames(home.left));
        }
        if (home.right != null) {
            home.var_names.addAll(getVarNames(home.right));
        }
        if (home.left == null && home.right == null) {
            if (home.value.charAt(0) == '\\' || home.value.charAt(0) == 'λ') {
                home.var_names.add(home.value.substring(1, home.value.length() - 1));
            } else {
                home.var_names.add(home.value);
            }
        }
        return home.var_names;
    }

    public boolean isBound() {
        return bound_vars.contains(this.value) || this.value.charAt(0) == '\\' || this.value.charAt(0) == 'λ';
    }

    public Node() {

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
            if (left.left == null && left.right == null && left.value.charAt(0) == '\\') {
                return "(" + "λ" + left.toString().substring(1) + "" + right.toString() + ")";
            }
            return "(" + left.toString() + " " + right.toString() + ")";
        } else if (left != null) {
            return left.toString();
        } else if (value != null) {
            return value;
        } else {
            return "";
        }
    }

    public Node clone() {  
        try{  
            String new_copy = this.toString();
            Parser parser = new Parser();
            Lexer lexer = new Lexer();
            return parser.parse(parser.preparse(lexer.tokenize(new_copy)), 0);
        } catch (Exception e) {
            return null; 
        }
    }


}
