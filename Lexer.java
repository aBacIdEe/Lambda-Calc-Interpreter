
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
	
	/*
	 * A lexer (or "tokenizer") converts an input into tokens that
	 * eventually need to be interpreted.
	 * 
	 * Given the input 
	 *    (\bat  .bat flies)cat  λg.joy! )
	 * you should output the ArrayList of strings
	 *    [(, \, bat, ., bat, flies, ), cat, \, g, ., joy!, )]
	 *
	 */
	public ArrayList<String> tokenize(String input) {
		ArrayList<String> tokens = new ArrayList<String>();

		String current_token = "";
		boolean is_comment = false;

		for (int i = 0; i < input.length(); i++) {
			char current_char = input.charAt(i);
			if (is_comment) {
				//  do nothing
			} else if (current_char == 'λ') {
				if (current_token.length() > 0) {
					tokens.add(current_token);
				}
				current_token = "";
				tokens.add("λ");
			} else if (current_char == '.') {
				if (current_token.length() > 0) {
					tokens.add(current_token);
				}
				current_token = "";
				tokens.add(".");
			} else if (current_char == '(') {
				if (current_token.length() > 0) {
					tokens.add(current_token);
				}
				current_token = "";
				tokens.add("(");
			} else if (current_char == ')') {
				if (current_token.length() > 0) {
					tokens.add(current_token);
				}
				current_token = "";
				tokens.add(")");
			} else if (current_char == ' ') {
				if (current_token.length() > 0) {
					tokens.add(current_token);
				}
				current_token = "";
			} else if (current_char == ';') {
				if (current_token.length() > 0) {
					tokens.add(current_token);
				}
				current_token = "";
				is_comment = true;
			} else {
				current_token += Character.toString(current_char);
			}
		}
		tokens.add(current_token);

		return tokens;
	}



}
