
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class Parser {
	
	/*
	 * Turns a set of tokens into an expression.  Comment this back in when you're ready.
	 */

	// Node root = new Node("Start");

	Node pointer;

	public Node parse(ArrayList<String> tokens, int start) {
		if (tokens.size() == start) { // base case
			return pointer;
		}

		if (pointer.left == null) { // add to the left (inherent precedence)
			if (tokens.get(start).equals("(")) {
				// find where the closing paren is
				int end = start;
				int depth = 0;
				while (!tokens.get(end).equals(")") && depth >= 0) {
					end++;
					if (tokens.get(end).equals("(")) {
						depth++;
					} else if (tokens.get(end).equals(")")) {
						depth--;
					}
				}
				Node temp = pointer;
				pointer.left = new Node("App");
				pointer = pointer.left;
				pointer = parse(new ArrayList<String>(tokens.subList(start + 1, end)), 0);
				pointer = temp;
				start = end;
			} else { // free variable
				pointer.left = new Node(tokens.get(start));
			}
		} else if (pointer.right == null) { // if input still available
			if (tokens.get(start).equals("(")) {
				// find where the closing paren is
				int end = start;
				int depth = 0;
				while (!tokens.get(end).equals(")") && depth >= 0) {
					end++;
					if (tokens.get(end).equals("(")) {
						depth++;
					} else if (tokens.get(end).equals(")")) {
						depth--;
					}
				}
				Node temp = pointer;
				pointer.right = new Node("App");
				pointer = pointer.right;
				pointer = parse(new ArrayList<String>(tokens.subList(start + 1, end)), 0);
				pointer = temp;
				start = end;
			} else { // free variable
				pointer.right = new Node(tokens.get(start));
			}
		} else { // make higher node
			pointer.above = new Node("App"); // make higher node with function that is current
			Node temp = pointer;
			pointer = pointer.above; // move up
			pointer.left = temp;
			start--; // offset jank to remain on same token
		}
		return parse(tokens, start + 1);
	}

	public ArrayList<String> preparse(ArrayList<String> tokens) {

		ArrayList<String> parens = new ArrayList<String>();

		for (String s: tokens) {
			parens.add(s);
		}

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
}
