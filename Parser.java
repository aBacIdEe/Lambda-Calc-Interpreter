
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Parser {
	
	/*
	 * Turns a set of tokens into an expression.  Comment this back in when you're ready.
	 */

	Node pointer;

	// have to implement different types of commands and build in some logic to the console.
	// two nodes can be compared using toStr
	HashMap<String, Node> reference = new HashMap<>();
	Set<String> var_names = new HashSet<String>();



	public Parser() {
		this.pointer = new Node();
		this.pointer.above = this.pointer;
	}



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
	


	private void insertAtChildNode(Node child, ArrayList<String> tokens) {
		child.above = pointer;
		pointer = child;
		pointer = parse(tokens, 0);
		pointer = pointer.above;
	}

	

	public Node storeAndParse(ArrayList<String> tokens) {
		ArrayList<String> whatToParse = new ArrayList<>(tokens.subList(2, tokens.size()));
		if (tokens.get(2).equals("run")) {
			Node temp = runAndParse(whatToParse);
			reference.put(tokens.get(0), temp);
			return temp;
		} else {
			whatToParse = preparse(whatToParse);
			Node temp = parse(whatToParse, 0);
			reference.put(tokens.get(0), temp);
			return temp;
		}
	}

// (\a1. (\r1. (((a r) a1 ) r1)))

	public Node runAndParse(ArrayList<String> tokens) { // reduce is NOT safe
		ArrayList<String> whatToParse = new ArrayList<>(tokens.subList(1, tokens.size()));
		whatToParse = preparse(whatToParse);
		Node original = parse(whatToParse, 0);
		var_names.clear();
		original.var_names.clear();
		Set<String> starting_var_names = Node.getVarNames(original);
		for (String name: starting_var_names) {
			var_names.add(name);
		}
		Node temp = (Node)original.clone();
		String temp_str_new = temp.toString();
		boolean done = false;
		while (!done) {
			System.err.print("\\: ");
			System.err.println(temp_str_new);
			String temp_str_old = temp_str_new;
			// Node.findBoundVars(temp);
			var_names.clear();
			temp = reduce(temp);
			temp_str_new = temp.toString();
			if (temp_str_old.equals(temp_str_new)) {
				done = true;
			}
		}
		return temp;
	}



	public String newVarName(String original) {
		int added_number = 1;
		while (var_names.contains(original + String.valueOf(added_number))) {
			added_number++;
		}
		var_names.add(original + String.valueOf(added_number));
		return original + String.valueOf(added_number);
	}



	public void traverseAndReplace(Node home, String token, String new_name, int depth) {
		// if depth == 0 and the left is a lambda and the value being replaced is the same
		// then continue traversing at depth = 1
		if (depth == 0) { // if it sees a lambda
			if (home.left != null
			&& !home.left.value.equals("") 
			&& ((home.left.value.charAt(0) == '\\' || home.left.value.charAt(0) == 'λ'))) {
				// if it's the correct value
				if (home.left.value.substring(1, home.left.value.length() - 1).equals(token)) {
					home.left.value = home.left.value.replace(token, new_name);
					traverseAndReplace(home.right, token, new_name, depth + 1);
				} else {
					traverseAndReplace(home.right, token, new_name, depth);
				}
			} else {
				if (home.left != null) {
					traverseAndReplace(home.left, token, new_name, depth);
				}
				if (home.right != null) {
					traverseAndReplace(home.right, token, new_name, depth);
				}
				if (home.left == null && home.right == null) {
					if (home.isBound()) {
						if (home.value.equals(token) ||
							((home.value.charAt(0) == '\\' || home.value.charAt(0) == 'λ') && home.value.substring(1, home.value.length() - 1).equals(token))) {
							home.value = home.value.replace(token, new_name);
						}
					}
				}
			}
			
		} else {
			if (home.left != null
			&& !home.left.value.equals("") 
			&& ((home.left.value.charAt(0) == '\\' || home.left.value.charAt(0) == 'λ'))
			&& home.left.value.substring(1, home.left.value.length() - 1).equals(token)) {
			// if you see the same thing again but on higher depth, do nothing
			
			} else {
				if (home.left != null) {
					traverseAndReplace(home.left, token, new_name, depth);
				}
				if (home.right != null) {
					traverseAndReplace(home.right, token, new_name, depth);
				}
				if (home.left == null && home.right == null) {
					if (home.isBound()) {
						if (home.value.equals(token) ||
							((home.value.charAt(0) == '\\' || home.value.charAt(0) == 'λ') && home.value.substring(1, home.value.length() - 1).equals(token))) {
							home.value = home.value.replace(token, new_name);
						}
					}
				}
			}
		}
	}



	public void betaReduce(Node free, String bound, Node home) { // free is copied each time
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
			if (home.right != null) {
				betaReduce(free, bound, home.right);
			}
			if (home.left != null) {
				betaReduce(free, bound, home.left);
			} 
		}
	}

	public boolean isLambda(String value) {
		if (value.length() > 1) {
			return value.charAt(0) == '\\' || value.charAt(0) == 'λ' || value.charAt(1) == '\\' || value.charAt(1) == 'λ';
		}
		if (value.length() > 0) {
			return value.charAt(0) == '\\' || value.charAt(0) == 'λ';
		}
		return false;
	}

	public Node reduce(Node home) { // called if a run is deteced
		// if home.left.left is a lambda, and all exists, then a substituion can be done;
		// since it's all done directly on the tree, 
		// need to make sure everything is still points to the correct thing

		// base cases first, as in return the same node if certain conditions are fulfilled
		// or else performs a recursive call
		// var_names = 
		if (home.left != null && home.right != null) { // single variable
			home.left = reduce(home.left); // reduce left as much as possible
			home.right = reduce(home.right); // reduce right as much as possible
			// alpha reduce anything first if left is a lambda func
			if (home.left.left != null && isLambda(home.left.toString())) {
				home.left.var_names.clear();
				Set<String> left = Node.getVarNames(home.left);
				home.right.var_names.clear();
				Set<String> right = Node.getVarNames(home.right);;
				left.retainAll(right);
				String[] intersection = left.toArray(new String[left.size()]);
				for (String var: intersection) { // replace bound variables in leftright
						Node top = home;
						while (top != top.above) {
							top = top.above;
						}
						String new_name = newVarName(var);
			
						// replace a bound var and its children
						// find usages inside the the new thing being plugged in
						// find usages in the thing we're plugging into
						// find parent usage of this var and sub
						// i.e. (f ... (f x) <-- (\f.f))
						top.bound_vars.clear();
						Node.findBoundVars(top);
						// always do something with the parts subbed in
						traverseAndReplace(home.right, var, new_name, 0);
						// do something with the parts subbed into if the parent is the same.
						// if (home.left != null && home.left.left != null && home.left.left.value.length() > 0 && home.left.left.value.substring(1, home.left.left.value.length() - 1).equals(var)) {
						traverseAndReplace(top, var, new_name, 0);
						// }
						System.err.println("a: " + top.toString());
				}
			}
			// or do beta reduction stuff if possible
			if (home.left.left != null && home.left.right != null) { // check for existence
				if (home.left.left.toString().charAt(0) == '\\' || home.left.left.toString().charAt(0) == 'λ') { // if it's a lambda
					// wow lambda func exists
					Node top = home;
					while (top != top.above) {
						top = top.above;
					}
					
					// String prev = home.toString();
					// betaReduce(home.right, home.left.left.value.substring(1, home.left.left.value.length() - 1), home.left.right);
					// String curr = home.toString();
					// while (!prev.equals(curr)) {
					// 	System.err.println(curr);
					// 	prev = home.toString();
					// 	betaReduce(home.right, home.left.left.value.substring(1, home.left.left.value.length() - 1), home.left.right);
					// 	curr = home.toString();
					// }
					betaReduce(home.right, home.left.left.value.substring(1, home.left.left.value.length() - 1), home.left.right);
					Node temp = home.left.right;
					temp.above = home.above;
					home = temp;
					System.err.println("b: " + top.toString());			
				}
			}
			
			
			return home;
		} else if (home.left != null) { //
			return reduce(home.left);
		} else if (home.right != null) {
			return reduce(home.right);
		} else {
			return home;
		}
	}



	public Node parse(ArrayList<String> tokens, int start) {
		// System.out.println("tokens: " + tokens);
		if (tokens.size() == start) { // base case
			Node ret = pointer;
			// reset for next thing to be parsed
			this.pointer = new Node();
			this.pointer.above = this.pointer;
			return ret;
		}
		// System.out.println("pointer: " + pointer.toString());

		String token = tokens.get(start);

		if (pointer.left == null) { // add to the left (inherent precedence)
			if (token.equals("(")) {
				int end = findMatchingParen(tokens, start);
				pointer.left = new Node();
				insertAtChildNode(pointer.left, new ArrayList<String>(tokens.subList(start + 1, end)));
				start = end;
			} else { // free variable
				if (reference.containsKey(token)) {
					pointer.left = (Node)reference.get(token).clone();
				} else {
					pointer.left = new Node(token);
				}
				pointer.left.above = pointer;
			}
		} else if (pointer.right == null) { // if input still available
			if (token.equals("(")) {
				int end = findMatchingParen(tokens, start);
				pointer.right = new Node();
				insertAtChildNode(pointer.right, new ArrayList<String>(tokens.subList(start + 1, end)));
				start = end;
			} else { // free variable
				if (reference.containsKey(token)) {
					pointer.right = (Node)reference.get(token).clone();
				} else {
					pointer.right = new Node(token);
				}
				pointer.right.above = pointer;
			}
		} else { // make higher node to be inserted between current node and above node
			Node temp = new Node(pointer); // make higher node with function that is current
			temp.above = pointer.above;
			if (pointer == pointer.above.left) { // change left side
				pointer.above.left = temp;
			} else { // change right side
				pointer.above.right = temp;
			}
			pointer.above = temp;
			pointer = pointer.above;
			start--; // offset jank to remain on same token
		}
		return parse(tokens, start + 1);
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

	

	public ArrayList<String> preparse(ArrayList<String> tokens) {
		ArrayList<String> parens = handleLambdas(tokens);
		parens = removeRedundantParens(parens);
		return parens;
	}
}
