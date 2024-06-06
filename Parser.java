
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Parser {
	
	/*
	 * Turns a set of tokens into an expression.
	 */

	Node pointer;

	// have to implement different types of commands and build in some logic to the console.
	// two nodes can be compared using toStr
	HashMap<String, Node> reference = new HashMap<>();
	Set<String> var_names = new HashSet<String>();
	ArrayList<String> bound_vars = new ArrayList<String>();

	public Parser() {
		this.pointer = new Node();
		this.pointer.above = this.pointer;
	}

	/*
	 * Helper Functions - mostly private facing
	 */

	private int findMatchingParen(ArrayList<String> tokens, int start) {
		int end = start + 1;
		int depth = 0;
		while (!tokens.get(end).equals(")") || depth > 0) {
			if (tokens.get(end).equals("(")) {
				depth++;
			} else if (tokens.get(end).equals(")")) {
				depth--;
			}
			end++;
			if (end >= tokens.size()) {
				return tokens.size() - 1;
			}
		}
		if (end != start) {
			return end;
		} else {
			return tokens.size() - 1;
		}
	}
	
	public void store(String var_name, Node root) {
		reference.put(var_name, root);
	}

	private String newVarName(String original) {
		int added_number = 1;
		while (var_names.contains(original + String.valueOf(added_number))) {
			added_number++;
		}
		var_names.add(original + String.valueOf(added_number));
		return original + String.valueOf(added_number);
	}

	private void traverseAndReplace(Node home, String token, String new_name) { // used by alpha reduction not beta reduction
		if (home.left != null) {
			traverseAndReplace(home.left, token, new_name);
		}
		if (home.right != null) {
			traverseAndReplace(home.right, token, new_name);
		}
		if (home.left == null && home.right == null) {
			if (home.value.equals(token) ||
				((home.value.charAt(0) == '\\' || home.value.charAt(0) == 'λ') && home.value.substring(1, home.value.length() - 1).equals(token))) {
				home.value = home.value.replace(token, new_name);
			}
		}
	}

	/*
	 * For everything reduction - reduces to minimum possible state
	 */

	public Node reduce(Node home) { // reduce is NOT safe
		var_names = Node.getAllVars(home);
		String oldString = home.toString();
		home = reduceOnce(home);
		String newString = home.toString();
		while (!oldString.equals(newString)) {
			System.out.println(oldString);
			oldString = newString;
			home = reduceOnce(home);
			newString = home.toString();
		}
		return home;
	}

	private Node reduceOnce(Node home) { // called if a run is deteced

		if (home.left != null) {
			home.left = reduceOnce(home.left);
		}
		
		if (home.right != null) {
			home.right = reduceOnce(home.right);
		}

		if (home.left != null && home.right != null && home.left.left != null && home.left.right != null && (home.left.left.value.startsWith("\\") || home.left.left.value.startsWith("λ"))) { // suitable for reduction
			alphaReduce(home);
			betaReduce(home.right, home.left.left.value.substring(1, home.left.left.value.length() - 1), home.left.right);
			Node temp = home.left.right;
			temp.above = home.above;
			home = temp;
		}

		return home;
	}

	private void alphaReduce(Node home) {
		Set<String> left = Node.getBoundVars(home.left);
		Set<String> right = Node.getFreeVars(home.right);
		left.retainAll(right); // intersection
		String[] intersection = left.toArray(new String[left.size()]);
		for (String var: intersection) { // replace bound variables in leftright
			String new_name = newVarName(var);
			traverseAndReplace(home.left.right, var, new_name);
		}
	}

	private void betaReduce(Node free, String bound, Node home) { // free is copied each time
		if (home.left == null & home.right == null) {
			if (home.value.equals(bound)) {
				Node temp = (Node)free.clone();
				temp.above = home.above;
				if (home.above.left == home) {
					home.above.left = temp;
				} else {
					home.above.right = temp;
				}
			}
		} else {
			if (home.left != null) {
				if (home.left != null && home.left.value.equals("λ" + bound + ".")) {

				} else {
					betaReduce(free, bound, home.left);
				}
			}
			if (home.right != null && home.left == null) {
				betaReduce(free, bound, home.right);
			}
			if (home.right != null && home.left != null) {
				if (home.left != null && home.left.value.equals("λ" + bound + ".")) {

				} else {
					betaReduce(free, bound, home.right);
				}
			}

		}
	}

	/*
	 * For everything parsing - constructs tree from tokens - labels free/bound vars
	 */

	public Node parse(ArrayList<String> tokens, int start) { // recursive implementation
		// System.out.println("tokens: " + tokens);
		if (tokens.size() == start) { // base case - starting index == end of tokens
			Node temp = pointer;
			pointer = new Node();
			pointer.above = pointer;
			return temp;
		}

		boolean has_new_bound = false;
		String token = tokens.get(start);
		if (token.startsWith("\\") || token.startsWith("λ")) {
			has_new_bound = true;
			bound_vars.add(token.substring(1, token.length() - 1));
		}

		// Could not find a clean way to refactor this

		if (pointer.left == null) { // add to the left (first priority)
			if (token.equals("(")) {
				int end = findMatchingParen(tokens, start);
				pointer.left = new Node();
				pointer.left.above = pointer;
				pointer = pointer.left;
				pointer = parse(new ArrayList<String>(tokens.subList(start + 1, end)), 0);
				pointer = pointer.above;
				start = end;
			} else { // lone variable
				if (reference.containsKey(token)) { // subs in for predefined definitions
					pointer.left = (Node)reference.get(token).clone();
				} else {
					pointer.left = new Node();
					pointer.left.value = token;
					if (bound_vars.contains(token) || token.startsWith("\\") || token.startsWith("λ")) {
						pointer.left.isFree = false;
					} else {
						pointer.left.isFree = true;
					}
				}
				pointer.left.above = pointer;
			}
		} else if (pointer.right == null) { // add to the right (second priority)
			if (token.equals("(")) {
				int end = findMatchingParen(tokens, start);
				pointer.right = new Node();
				pointer.right.above = pointer;
				pointer = pointer.right;
				pointer = parse(new ArrayList<String>(tokens.subList(start + 1, end)), 0);
				pointer = pointer.above;
				start = end;
			} else { // lone variable
				if (reference.containsKey(token)) {
					pointer.right = (Node)reference.get(token).clone();
				} else {
					pointer.right = new Node();
					pointer.right.value = token;
					if (bound_vars.contains(token) || token.startsWith("\\") || token.startsWith("λ")) {
						pointer.right.isFree = false;
					} else {
						pointer.right.isFree = true;
					}
				}
				pointer.right.above = pointer;
			}
		} else { // does not process token, instead make new above node to be inserted between current node and current above node
			Node temp = new Node(); // make higher node with function that is current
			temp.left = pointer;
			temp.above = pointer.above;
			if (pointer == pointer.above.left) { // change left side
				pointer.above.left = temp;
			} else { // change right side
				pointer.above.right = temp;
			}
			pointer.above = temp;

			pointer = pointer.above; // mvoes pointer up after
			start--; // offset jank to remain on same token
		}

		if (has_new_bound) {
			bound_vars.remove(bound_vars.size() - 1);
		}

		return parse(tokens, start + 1);
	}

	/*
	 * For everything preparsing - corrects parentheses
	 */

	public ArrayList<String> preparse(ArrayList<String> tokens) {
		ArrayList<String> parens = handleLambdas(tokens);
		parens = removeRedundantParens(parens);
		return parens;
	}

	private ArrayList<String> handleLambdas(ArrayList<String> tokens) {
		int index = 0;
		while (index < tokens.size()) {
			if (tokens.get(index).equals("\\") || tokens.get(index).equals("λ")) {
				tokens.add(index, "(");
				int end = findMatchingParen(tokens, index);
				if (end == tokens.size() - 1) {
					tokens.add(")");
				} else {
					tokens.add(end, ")");
				}
				index++;
			}
			index++;
		};
		tokens.add(0, "(");
		tokens.add(")");

		for (int i = 0; i < tokens.size()-2; i++) {
			if (tokens.get(i).equals("\\") || tokens.get(i).equals("λ")) {
				tokens.set(i, tokens.get(i) + tokens.get(i+1) + tokens.get(i+2));
				tokens.remove(i+1);
				tokens.remove(i+1);
				tokens.add(i+1, "(");
				int end = findMatchingParen(tokens, i);
				tokens.add(end, ")");
			}
		}

		return tokens;
	}

	private ArrayList<String> removeRedundantParens(ArrayList<String> tokens) {
		// removes redundant parens
		ArrayList<ParenPair> parens = new ArrayList<>();
		ArrayList<Integer> to_be_removed = new ArrayList<Integer>();
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("(")) {
				parens.add(new ParenPair(i));
				parens.get(parens.size() - 1).end = findMatchingParen(tokens, i);
			}
		}
		parens.remove(0); // removes the outermost one
		for (ParenPair pair: parens) {
			if (tokens.get(pair.start - 1).equals("(") && tokens.get(pair.end + 1).equals(")")) {
				to_be_removed.add(pair.start);
				to_be_removed.add(pair.end);
			}
		}
		Collections.sort(to_be_removed);
		for (int i = to_be_removed.size() - 1; i >= 0; i--) {
			tokens.remove((int)to_be_removed.get(i));
		}

		// remove redundant parens in the form ((a)) --> a
		int pointer = 0;
		while (pointer < tokens.size() - 2) {
			if (tokens.get(pointer).equals("(") && tokens.get(pointer + 2).equals(")")) {
				tokens.remove(pointer + 2);
				tokens.remove(pointer);
				pointer = 0;
			} else {
				pointer++;
			}
		}

		return tokens;
	}

}
