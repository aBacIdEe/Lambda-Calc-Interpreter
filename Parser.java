
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class Parser {
	
	/*
	 * Turns a set of tokens into an expression.  Comment this back in when you're ready.
	 */

	Node pointer;

	// have to implement different types of commands and build in some logic to the console.
	// two nodes can be compared using toStr
	HashMap<String, Node> reference = new HashMap<>();



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

	public Node runAndParse(ArrayList<String> tokens) {
		ArrayList<String> whatToParse = new ArrayList<>(tokens.subList(1, tokens.size()));
		whatToParse = preparse(whatToParse);
		// System.out.print("Preparsed: ");
		// System.out.println(whatToParse);
		Node temp = parse(whatToParse, 0);
		temp = reduce(temp);
		// System.err.println(temp);
		while (!temp.toString().equals(reduce(temp).toString())) {
			temp = reduce(temp);
			// System.err.println(temp);
		}
		return temp;
	}



	public Node reduce(Node home) { // called if a run is deteced
		// if home.left.left is a lambda, and all exists, then a substituion can be done;
		// since it's all done directly on the tree, 
		// need to make sure everything is still points to the correct thing

		// base cases first, as in return the same node if certain conditions are fulfilled
		// or else performs a recursive call
		if (home.left != null && home.right != null) { // single variable
			home.left = reduce(home.left); // reduce left as much as possible
			home.right = reduce(home.right); // reduce right as much as possible
			if (home.left.left != null && home.left.right != null) { // check for existence
				if (home.left.left.toString().charAt(0) == '\\') { // if it's a lambda
					if (home.left.right != null) { // wow lambda func exists
						// time to test for intersection
						Set<String> leftright = Node.getVarNames(home.left.right);
						Set<String> right = Node.getVarNames(home.right);

						leftright.retainAll(right);

						String[] intersection = leftright.toArray(new String[leftright.size()]);
						String predicate = home.left.toString();
						for (String var: intersection) { // replace bound variables in leftright
							predicate = predicate.replace(var, var + "1");
						}
						// alpha reduction is done
						int lindex = 2;
						int rindex = predicate.indexOf('.');
						String lambda_term = predicate.substring(lindex, rindex);
						// lambda_term = lambda_term.substring(1, lambda_term.length() - 1);
						String free_var = home.right.toString();
						predicate = predicate.substring(rindex + 2, predicate.length() - 1);
						predicate = predicate.replace(lambda_term, free_var);
						// beta reduction is done, time to reparse
						Lexer lexer = new Lexer();
						Parser parser = new Parser();
						parser.pointer = new Node("Start");
						parser.pointer.above = parser.pointer;
						home.left.right = parser.parse(parser.preparse(lexer.tokenize(predicate)), 0);
						// reorganize pointers
						home = home.left.right;
					}
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
			return pointer;
		}
		// System.out.println("pointer: " + pointer.toString());

		String token = tokens.get(start);

		if (pointer.left == null) { // add to the left (inherent precedence)
			if (token.equals("(")) {
				int end = findMatchingParen(tokens, start);
				pointer.left = new Node("App");
				insertAtChildNode(pointer.left, new ArrayList<String>(tokens.subList(start + 1, end)));
				start = end;
			} else { // free variable
				if (reference.containsKey(token)) {
					pointer.left = reference.get(token);
				} else {
					pointer.left = new Node(token);
				}
				pointer.left.above = pointer;
			}
		} else if (pointer.right == null) { // if input still available
			if (token.equals("(")) {
				int end = findMatchingParen(tokens, start);
				pointer.right = new Node("App");
				insertAtChildNode(pointer.right, new ArrayList<String>(tokens.subList(start + 1, end)));
				start = end;
			} else { // free variable
				if (reference.containsKey(token)) {
					pointer.right = reference.get(token);
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
			temp.value = "App";
			start--; // offset jank to remain on same token
		}
		// System.out.println("Pointer: " + pointer);
		// System.out.println("Above: " + pointer.above);
		return parse(tokens, start + 1);
	}



	private ArrayList<String> handleLambdas(ArrayList<String> tokens) {

		int index = 0;
		while (index < tokens.size()) {
			if (tokens.get(index).equals("\\")) {
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
			if (tokens.get(i).equals("\\")) {
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
		System.err.println("lambdas handled");
		System.err.println(parens);
		parens = removeRedundantParens(parens);
		System.err.println("extra parens removed");
		System.err.println(parens);
		System.err.println();
		return parens;
	}
}
