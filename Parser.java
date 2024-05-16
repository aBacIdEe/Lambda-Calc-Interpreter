
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

public class Parser {
	
	/*
	 * Turns a set of tokens into an expression.  Comment this back in when you're ready.
	 */

	Node pointer;

	// have to implement different types of commands and build in some logic to the console.
	// two nodes can be compared using toStr
	HashMap<String, Node> reference = new HashMap<>();



	private int findMatchingParen(ArrayList<String> tokens, int start) {
		int end = start;
		int depth = 0;
		while (!tokens.get(end).equals(")") || depth >= 0) {
			end++;
			if (tokens.get(end).equals("(")) {
				depth++;
			} else if (tokens.get(end).equals(")")) {
				depth--;
			}
		}
		return end;
	}
	


	private void insertAtChildNode(Node child, ArrayList<String> tokens) {
		child.above = pointer;
		pointer = child;
		pointer = parse(tokens, 0);
		pointer = pointer.above;
	}

	public Node storeAndParse(ArrayList<String> tokens) {
		ArrayList<String> whatToParse = new ArrayList<>(tokens.subList(2, tokens.size()));
		whatToParse = preparse(whatToParse);
		Node temp = parse(whatToParse, 0);
		//System.out.println(temp);
		//System.out.println(tokens.get(0));
		reference.put(tokens.get(0), temp);
		return temp;
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
		ArrayList<String> parens = new ArrayList<String>();

		int extra = 0;
		parens.add("(");
		for (String s: tokens) {
			if (s.equals("\\")) { // if it's a lambda
				parens.add("("); // put a left paren first
				extra++;
			} else if (s.equals(")") && extra > 0) {
				parens.add(")");
				extra--;
			}
			parens.add(s);
		}
		parens.add(")");
		for (int i = 0; i < extra; i++) {
			parens.add(")");
		}
		for (int i = 0; i < parens.size()-2; i++) {
			if (parens.get(i).equals("\\")) {
				parens.set(i, parens.get(i) + parens.get(i+1) + parens.get(i+2));
				parens.remove(i+1);
				parens.remove(i+1);
				parens.add(i+1, "(");
				int end = findMatchingParen(parens, i);
				parens.add(end, ")");
			}
		}

		return parens;
	}



	private ArrayList<String> removeRedundantParens(ArrayList<String> parens) {
		// removes redundant parens
		Stack<ParenPair> paren_stack = new Stack<ParenPair>();
		paren_stack.add(new ParenPair(-1)); // prevents peeking into empty
		ArrayList<Integer> to_be_removed = new ArrayList<Integer>();
		for (int i = 0; i < parens.size(); i++) {
			if (parens.get(i).equals("(")) {
				paren_stack.add(new ParenPair(i));
			} else if (parens.get(i).equals(")")) {
				if (!paren_stack.peek().necessary) {
					to_be_removed.add(paren_stack.peek().start);
					to_be_removed.add(i);
					paren_stack.pop();
				} else {
					paren_stack.pop();
				}
			} else {
				paren_stack.peek().necessary = true;
			}
		}
		Collections.sort(to_be_removed);
		for (int i = to_be_removed.size() - 1; i >= 0; i--) {
			parens.remove((int)to_be_removed.get(i));
		}

		// remove redundant parens in the form ((a)) --> a
		int pointer = 0;
		while (pointer < parens.size() - 2) {
			if (parens.get(pointer).equals("(") && parens.get(pointer + 2).equals(")")) {
				parens.remove(pointer + 2);
				parens.remove(pointer);
				pointer = 0;
			} else {
				pointer++;
			}
		}

		return parens;
	}



	public ArrayList<String> preparse(ArrayList<String> tokens) {
		ArrayList<String> parens = handleLambdas(tokens);
		parens = removeRedundantParens(parens);
		return parens;
	}
}
