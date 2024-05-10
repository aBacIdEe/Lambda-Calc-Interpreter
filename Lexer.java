
import java.util.ArrayList;

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
	private final String special_characters = "λ\\.()=";

	public ArrayList<String> tokenize(String input) {
		ArrayList<String> tokens = new ArrayList<String>();

		String current_token = "";
		boolean is_comment = false;

		for (int i = 0; i < input.length(); i++) {
			char current_char = input.charAt(i);
			if (is_comment) {
				//  do nothing
			} else if (special_characters.indexOf(current_char) != -1) {
				if (current_token.length() > 0) {
					tokens.add(current_token);
				}
				current_token = "";
				tokens.add(Character.toString(current_char));
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
		if (current_token.length() > 0) {
			tokens.add(current_token);
		}

		return tokens;
	}




}
