
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class Parser {
	
	/*
	 * Turns a set of tokens into an expression.  Comment this back in when you're ready.
	 */

	Node pointer;
	
	public Node parse(ArrayList<String> tokens, int start) {
		// System.out.println("tokens: " + tokens);
		if (tokens.size() == start) { // base case
			return pointer;
		}
		// System.out.println("pointer: " + pointer.toString());

		if (pointer.left == null) { // add to the left (inherent precedence)
			if (tokens.get(start).equals("(")) {
				// find where the closing paren is
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
				// Node temp = pointer;
				pointer.left = new Node("App");
				pointer.left.above = pointer;
				pointer = pointer.left;
				pointer = parse(new ArrayList<String>(tokens.subList(start + 1, end)), 0);
				pointer = pointer.above;
				start = end;
			} else { // free variable
				pointer.left = new Node(tokens.get(start));
				pointer.left.above = pointer;
			}
		} else if (pointer.right == null) { // if input still available
			if (tokens.get(start).equals("(")) {
				// find where the closing paren is
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
				pointer.right = new Node("App");
				pointer.right.above = pointer;
				pointer = pointer.right;
				pointer = parse(new ArrayList<String>(tokens.subList(start + 1, end)), 0);
				pointer = pointer.above;
				start = end;
			} else { // free variable
				pointer.right = new Node(tokens.get(start));
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
		System.out.println("Pointer: " + pointer);
		System.out.println("Above: " + pointer.above);
		return parse(tokens, start + 1);
	}

	public ArrayList<String> preparse(ArrayList<String> tokens) {

		ArrayList<String> parens = new ArrayList<String>();

		int extra = 0;

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
		for (int i = 0; i < extra; i++) {
			parens.add(")");
		}
		for (int i = 0; i < parens.size()-2; i++) {
			if (parens.get(i).equals("\\")) {
				parens.set(i, parens.get(i) + parens.get(i+1) + parens.get(i+2));
				parens.remove(i+1);
				parens.remove(i+1);
				parens.add(i+1, "(");
				int end = i;
				int depth = 0;
				while (!parens.get(end).equals(")") && depth >= 0) {
					end++;
					if (parens.get(end).equals("(")) {
						depth++;
					} else if (parens.get(end).equals(")")) {
						depth--;
					}
				}
				parens.add(end, ")");
			}
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
