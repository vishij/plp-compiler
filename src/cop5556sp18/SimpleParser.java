package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */

import static cop5556sp18.Scanner.Kind.*;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;

public class SimpleParser {

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}

	/*
	 * Program ::= Identifier Block
	 */
	public void program() throws SyntaxException {
		match(IDENTIFIER);
		block();
	}

	/*
	 * Block ::= { ( (Declaration | Statement) ; )* }
	 */

	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = { KW_input, KW_write, IDENTIFIER, KW_while, KW_if, KW_show, KW_sleep };
	Kind[] color = { KW_red, KW_green, KW_blue, KW_alpha };
	Kind[] functionName = { KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r,
			KW_int, KW_float, KW_width, KW_height };
	Kind[] predefinedName = { KW_Z, KW_default_height, KW_default_width };

	public void block() throws SyntaxException {
		match(LBRACE);
		while (isKind(firstDec) | isKind(firstStatement)) {
			if (isKind(firstDec)) {
				declaration();
			} else if (isKind(firstStatement)) {
				statement();
			}
			match(SEMI);
		}
		match(RBRACE);
	}

	public void declaration() throws SyntaxException {
		switch (t.kind) {
		case KW_image: {
			match(KW_image);
			match(IDENTIFIER);
			match(LSQUARE);
			expression();
			// TODO: can remove if check and directly call match for the rest two also
			if (isKind(COMMA)) {
				match(COMMA);
			}
			expression();
			if (isKind(RSQUARE)) {
				match(RSQUARE);
			}
		}
			break;
		case KW_int:
		case KW_float:
		case KW_boolean:
		case KW_filename:
			match(t.kind);
			match(IDENTIFIER);
			break;
		default:
			throw new SyntaxException(t,
					"Wrong start of declaration. Should be int, float, boolean, image, or filename");
		}
	}

	public void statement() throws SyntaxException {
		switch (t.kind) {
		case KW_input: {
			statementInput();
		}
			break;
		case KW_write: {
			statementWrite();
		}
			break;
		case KW_while: {
			statementWhile();
		}
			break;
		case KW_if: {
			statementIf();
		}
			break;
		case KW_show: {
			match(KW_show);
			expression();
		}
			break;
		case KW_sleep: {
			match(KW_sleep);
			expression();
		}
			break;
			// TODO: refactor
		// LHS statement cases: LHS ::= IDENTIFIER | IDENTIFIER PixelSelector | Color (
		// IDENTIFIER PixelSelector )
		case IDENTIFIER: {
			match(IDENTIFIER);
			if (isKind(LSQUARE)) {
				pixelSelector();
			}
		}
			break;
		case KW_red:
		case KW_green:
		case KW_blue:
		case KW_alpha: {
			match(t.kind);
			match(LPAREN);
			match(IDENTIFIER);
			pixelSelector();
			match(RPAREN);
		}
			break;
		default:
			throw new SyntaxException(t, "Syntax Error: Wrong statement syntax for: " + t.getText() + " at position: "
					+ t.posInLine() + " in line: " + t.line());
		}
	}

	// TODO: make statementIf and statementWhile methods generic
	private void statementIf() throws SyntaxException {
		match(Kind.KW_if);
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
	}

	private void statementWhile() throws SyntaxException {
		match(KW_while);
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
	}

	private void statementInput() throws SyntaxException {
		match(KW_input);
		match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		expression();
	}

	public void statementWrite() throws SyntaxException {
		match(KW_write);
		match(IDENTIFIER);
		match(KW_to);
		match(IDENTIFIER);
	}

	private void expression() throws SyntaxException {
		orExpression();
		if(isKind(OP_QUESTION)) {
			match(OP_QUESTION);
			expression();
			match(OP_COLON);
			expression();
		}
//		switch (t.kind) {
//		case OP_PLUS:
//		case OP_MINUS:
//		case OP_EXCLAMATION:
//		case INTEGER_LITERAL:
//		case BOOLEAN_LITERAL:
//		case FLOAT_LITERAL:
//		case LPAREN:
//		case IDENTIFIER:
//		case LPIXEL:
//		case KW_sin:
//		case KW_cos:
//		case KW_atan:
//		case KW_abs:
//		case KW_log:
//		case KW_cart_x:
//		case KW_cart_y:
//		case KW_polar_a:
//		case KW_polar_r:
//		case KW_int:
//		case KW_float:
//		case KW_width:
//		case KW_height:
//		case KW_red:
//		case KW_green:
//		case KW_blue:
//		case KW_alpha:
//			match(t.kind);
//			break;
//		case KW_Z:
//		case KW_default_height:
//		case KW_default_width:
//			match(t.kind);
//			break;
//		default:
//			throw new SyntaxException(t, "Syntax Error: Wrong expression syntax.");
//		}
	}

	private void orExpression() throws SyntaxException {
		andExpression();
		while(isKind(OP_OR)) {
			match(OP_OR);
			andExpression();
		}
	}

	private void andExpression() throws SyntaxException {
		eqExpression();
		while(isKind(OP_AND)) {
			match(OP_AND);
			eqExpression();
		}
	}

	private void eqExpression() {
		relExpression();
		while(isKind(OP_EQ) || isKind(OP_NEQ)) {
			match(t.kind);
		}
	}

	private void relExpression() {
		// TODO Auto-generated method stub
		
	}

	private void pixelConstructor() throws SyntaxException {
		if (isKind(LPIXEL)) {
			match(LPIXEL);
			expression();
			match(COMMA);
			expression();
			match(COMMA);
			expression();
			match(COMMA);
			expression();
			match(RPIXEL);
		}
	}

	private void pixelExpression() throws SyntaxException {
		if (isKind(IDENTIFIER)) {
			match(IDENTIFIER);
			pixelSelector();
		}
	}

	private void pixelSelector() throws SyntaxException {
		if (isKind(LSQUARE)) {
			match(LSQUARE);
			expression();
			match(COMMA);
			expression();
			match(RSQUARE);
		}
	}

	private void functionName() throws SyntaxException {
		// sin | cos | atan | abs | log | cart_x | cart_y | polar_a | polar_r
		// int | float | width | height | Color
		// red | green | blue | alpha
		switch (t.kind) {
		case KW_sin:
		case KW_cos:
		case KW_atan:
		case KW_abs:
		case KW_log:
		case KW_cart_x:
		case KW_cart_y:
		case KW_polar_a:
		case KW_polar_r:
		case KW_int:
		case KW_float:
		case KW_width:
		case KW_height:
		case KW_red:
		case KW_green:
		case KW_blue:
		case KW_alpha:
			match(t.kind);
			break;
		default:
			throw new SyntaxException(t, "Syntax Error: Wrong start of functionName.");
		}
	}

	private void predefinedName() throws SyntaxException {
		switch (t.kind) {
		case KW_Z:
		case KW_default_height:
		case KW_default_width:
			match(t.kind);
			break;
		default:
			throw new SyntaxException(t, "Syntax Error. Wrong start of functionName.");
		}
	}

	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}

	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t, "Syntax Error"); // TODO give a better error message!
	}

	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind(EOF)) {
			throw new SyntaxException(t, "Syntax Error"); // TODO give a better error message!
			// Note that EOF should be matched by the matchEOF method which is called only
			// in parse().
			// Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t, "Syntax Error"); // TODO give a better error message!
	}

}
