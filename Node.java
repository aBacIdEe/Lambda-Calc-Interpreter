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

    private Node cloneRecurse() {
        Node home = new Node();
        if (this.left != null) {
            home.left = this.left.cloneRecurse();
            home.left.above = home;
        }
        if (this.right != null) {
            home.right = this.right.cloneRecurse();
            home.right.above = home;
        }
        home.isFree = this.isFree;
        home.value = this.value;

        return home;
    }

    public Node clone() {  
        try {  
            Node temp = cloneRecurse();
            temp.above = temp;
            return temp;
        } catch (Exception e) {
            return null; 
        }
    }


}
