import java.util.HashSet;
import java.util.Set;

public class Node {
    
    public Node left;
    public Node right;
    public Node above;
    public String value = "";
    public boolean isFree;

    public static Set<String> getBoundVars(Node home) {
        return getVars(home, new HashSet<String>(), false, false);
    }

    public static Set<String> getFreeVars(Node home) {
        return getVars(home, new HashSet<String>(), false, true);
    }

    public static Set<String> getAllVars(Node home) {
        return getVars(home, new HashSet<String>(), true, true);
    }

    private static Set<String> getVars(Node home, Set<String> vars, boolean get_all, boolean get_free) {
        if (home.left != null) {
            vars.addAll(getVars(home.left, vars, get_all, get_free));
        }
        if (home.right != null) {
            vars.addAll(getVars(home.right, vars, get_all, get_free));
        }
        if (home.left == null && home.right == null && ((get_all) || (get_free == home.isFree))) {
            if (home.value.startsWith("\\") || home.value.startsWith("λ")) {
                vars.add(home.value.substring(1, home.value.length() - 1));
            } else {
                vars.add(home.value);
            }
        }
        return vars;
    }

    public Node() {

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
        try {  
            String new_copy = this.toString();
            Parser parser = new Parser();
            Lexer lexer = new Lexer();
            return parser.parse(parser.preparse(lexer.tokenize(new_copy)), 0).left; // this line is complete jank, i have no idea why this is the case
        } catch (Exception e) {
            return null; 
        }
    }


}
