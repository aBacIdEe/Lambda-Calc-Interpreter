
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Console {
	private static Scanner in;

	private static Lexer lexer = new Lexer();
	private static Parser parser = new Parser();
	
	private static String interpretCommand(String input) { // returns output
		ArrayList<String> tokens = lexer.tokenize(input);
		String var_name = ""; // only effective if storing
		boolean should_store = false;
		boolean should_reduce = false;

		if (tokens.size() >= 3 && tokens.get(1).equals("=")) { // checks for "="
			if (parser.reference.containsKey(tokens.get(0))) {
				return tokens.get(0) + " is already defined.";
			} else {
				var_name = tokens.get(0);
				tokens.remove(0);
				tokens.remove(0);
				should_store = true;
			}
		}

		if (tokens.size() == 3 && tokens.get(0).equals("populate")) { // checks for populate x y
			int firstNum = Integer.parseInt(tokens.get(1));
			int lastNum = Integer.parseInt(tokens.get(2));

			String firstString = "(\\f.\\x.";
			for (int i = 0; i < firstNum; i++) {
				firstString += "(f";
			}
			firstString += " x)";
			for (int i = 0; i < firstNum; i++) {
				firstString += ")";
			}

			String prevString = firstString;
			int prevNum = firstNum;

			for (int i = firstNum; i <= lastNum; i++) { //loop through and increase each number until the end of the range
				parser.store(String.valueOf(prevNum), parser.parse(parser.preparse(lexer.tokenize(prevString))));
				
				prevString = prevString.substring(0, firstNum*2+7) + "(f" + prevString.substring(firstNum*2+7) + ")";
				//System.out.println("new String: " + prevString);
				prevNum++;

			}

			return "Populated numbers from " + tokens.get(1) + " to " + tokens.get(2);
		}

		if (tokens.size() >= 2 && tokens.get(0).equals("run")) { // checks for "run"
			tokens.remove(0);
			should_reduce = true;
		}

		tokens = parser.preparse(tokens);
		Node root_node = parser.parse(tokens);

		if (should_reduce) {
			parser.cleanNodeTree(root_node);
			root_node = parser.reduce(root_node);
		}

		if (should_store) {
			parser.store(var_name, root_node);
		}

		String output_expression = root_node.toString();

		if (should_store) {
			return "Added " + output_expression + " as " + var_name;
		} else {
			// loop through the list of stored items to see if the output is a stored item
			for (int i = 0; i < parser.referencedItem.size(); i++) {
				if (parser.referencedItem.get(i).replace(" ", "").equals(output_expression.replace(" ", "")) && !parser.referenceItem.get(i).equals(var_name)) {
					output_expression = parser.referenceItem.get(i);
				}
			}
			return output_expression;
		}
	}

	public static void main(String[] args) {
		in = new Scanner (System.in);
		
		String input = cleanConsoleInput();  // see comment
		while (!input.equalsIgnoreCase("exit")) {
			String output = interpretCommand(input);
			System.out.println(output);
			input = cleanConsoleInput();
			// lexer = new Lexer();
			// parser = new Parser();
		}
		System.out.println("Goodbye!");
	}

	
	
	/*
	 * Collects user input, and ...
	 * ... does a bit of raw string processing to (1) strip away comments,  
	 * (2) remove the BOM character that appears in unicode strings in Windows,
	 * (3) turn all weird whitespace characters into spaces,
	 * and (4) replace all λs with backslashes.
	 */
	
	private static String cleanConsoleInput() {
		System.out.print("> ");
		String raw = in.nextLine();
		String deBOMified = raw.replaceAll("\uFEFF", ""); // remove Byte Order Marker from UTF

		String clean = removeWeirdWhitespace(deBOMified);
		
		return clean.replaceAll("λ", "\\\\");
	}
	
	
	public static String removeWeirdWhitespace(String input) {
		String whitespace_chars = 
				""           // dummy empty string for homogeneity
				+ "\\u0009"  // CHARACTER TABULATION
				+ "\\u000A"  // LINE FEED (LF)
				+ "\\u000B"  // LINE TABULATION
				+ "\\u000C"  // FORM FEED (FF)
				+ "\\u000D"  // CARRIAGE RETURN (CR)
				+ "\\u0020"  // SPACE
				+ "\\u0085"  // NEXT LINE (NEL) 
				+ "\\u00A0"  // NO-BREAK SPACE
				+ "\\u1680"  // OGHAM SPACE MARK
				+ "\\u180E"  // MONGOLIAN VOWEL SEPARATOR
				+ "\\u2000"  // EN QUAD 
				+ "\\u2001"  // EM QUAD 
				+ "\\u2002"  // EN SPACE
				+ "\\u2003"  // EM SPACE
				+ "\\u2004"  // THREE-PER-EM SPACE
				+ "\\u2005"  // FOUR-PER-EM SPACE
				+ "\\u2006"  // SIX-PER-EM SPACE
				+ "\\u2007"  // FIGURE SPACE
				+ "\\u2008"  // PUNCTUATION SPACE
				+ "\\u2009"  // THIN SPACE
				+ "\\u200A"  // HAIR SPACE
				+ "\\u2028"  // LINE SEPARATOR
				+ "\\u2029"  // PARAGRAPH SEPARATOR
				+ "\\u202F"  // NARROW NO-BREAK SPACE
				+ "\\u205F"  // MEDIUM MATHEMATICAL SPACE
				+ "\\u3000"; // IDEOGRAPHIC SPACE 
		Pattern whitespace = Pattern.compile(whitespace_chars);
		Matcher matcher = whitespace.matcher(input);
		String result = input;
		if (matcher.find()) {
			result = matcher.replaceAll(" ");
		}

		return result;
	}

}
