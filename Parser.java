
import java.text.ParseException;
import java.util.ArrayList;

public class Parser {
	
	/*
	 * Turns a set of tokens into an expression.  Comment this back in when you're ready.
	 */

	Node pointer = new Node();

	// public Node parse(ArrayList<String> tokens, int start) throws ParseException {
	// 	if (tokens.size() == 0) {
	// 		return pointer;
	// 	} else if (tokens.get(0) == ")") {

	// 	} else if (tokens.get(0) == "(") {
			
	// 	} else if (tokens.get(index:0) == "\\") {

	// 	} else {
	// 		pointer.function = new Node();
	// 		pointer.function.value = tokens.get(0);
	// 	}

	public ArrayList<String> preparse(ArrayList<String> tokens) {
		ArrayList<String> parens = new ArrayList<String>();
		int extras = 0;
		for (int i = tokens.size() - 1; i >= 0; i--) {
			String temp = tokens.get(i);
			if (temp == "(" || temp == ")") {
				parens.add(0, temp);
				i++;
			} else {
				parens.add(0, ")");
				parens.add(0, temp);
				extras++;
			}
		}
		for (int i = 0; i < extras; i++) {
			parens.add(0, "(");
		}

		System.out.println(parens);

		int pointer = 0;
		while (pointer < parens.size() - 2) {
			if (parens.get(pointer).equals("(") && parens.get(pointer + 1).equals(")")) {
				parens.remove(pointer);
				parens.remove(pointer);
				pointer = 0;
			} else {
				pointer++;
			}
		}
		pointer = 0;
		System.out.println(parens);
		while (pointer < parens.size() - 2) {
			if (parens.get(pointer).equals("(") && parens.get(pointer + 2).equals(")")) {
				parens.remove(pointer);
				pointer++;
				parens.remove(pointer);
				pointer = 0;
			} else {
				pointer++;
			}
		}
		return parens;
	}
}

// a b (c d) --> c d