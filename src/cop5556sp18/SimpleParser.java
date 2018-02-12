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

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;

import static cop5556sp18.Scanner.Kind.*;

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

    Kind[] firstDec = {KW_int, KW_boolean, KW_image, KW_float, KW_filename};
    Kind[] firstStatement = {KW_input, KW_write, IDENTIFIER, KW_while, KW_if, KW_show, KW_sleep};
    Kind[] color = {KW_red, KW_green, KW_blue, KW_alpha};
    Kind[] functionName = {KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r,
            KW_int, KW_float, KW_width, KW_height};
    Kind[] predefinedName = {KW_Z, KW_default_height, KW_default_width};

    public void block() throws SyntaxException {
        match(LBRACE);
        while (isKind(firstDec) | isKind(firstStatement) || isKind(color)) {
            if (isKind(firstDec)) {
                declaration();
            } else if (isKind(firstStatement) || isKind(color)) {
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
                if (isKind(LSQUARE)) {
                    match(LSQUARE);
                    expression();
                    match(COMMA);
                    expression();
                    match(RSQUARE);
                }
            }
            break;
            case KW_int:
            case KW_float:
            case KW_boolean:
            case KW_filename: {
                match(t.kind);
                match(IDENTIFIER);
            }
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
            case IDENTIFIER:
            case KW_red:
            case KW_green:
            case KW_blue:
            case KW_alpha: {
                statementAssignment();
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

    private void statementAssignment() throws SyntaxException {
        lhs();
        match(OP_ASSIGN);
        expression();
    }

    private void lhs() throws SyntaxException {
        if (isKind(IDENTIFIER)) {
            match(IDENTIFIER);
            if (isKind(LSQUARE)) {
                pixelSelector();
            }
        } else if (isKind(color)) {
            match(t.kind);
            match(LPAREN);
            match(IDENTIFIER);
            pixelSelector();
            match(RPAREN);
        } else {
            throw new SyntaxException(t, "Syntax Error: Wrong statement syntax for: " + t.getText() + " at position: "
                    + t.posInLine() + " in line: " + t.line());
        }
    }

    public void statementWrite() throws SyntaxException {
        match(KW_write);
        match(IDENTIFIER);
        match(KW_to);
        match(IDENTIFIER);
    }

    private void statementInput() throws SyntaxException {
        match(KW_input);
        match(IDENTIFIER);
        match(KW_from);
        match(OP_AT);
        expression();
    }

    private void expression() throws SyntaxException {
        orExpression();
        if (isKind(OP_QUESTION)) {
            match(OP_QUESTION);
            expression();
            match(OP_COLON);
            expression();
        }
    }

    private void orExpression() throws SyntaxException {
        andExpression();
        while (isKind(OP_OR)) {
            match(OP_OR);
            andExpression();
        }
    }

    private void andExpression() throws SyntaxException {
        eqExpression();
        while (isKind(OP_AND)) {
            match(OP_AND);
            eqExpression();
        }
    }

    private void eqExpression() throws SyntaxException {
        relExpression();
        while (isKind(OP_EQ) || isKind(OP_NEQ)) {
            // check if, if else if needed?
            match(t.kind);
            relExpression();
        }
    }

    private void relExpression() throws SyntaxException {
        addExpression();
        while (isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE) || isKind(OP_GE)) {
            match(t.kind);
            addExpression();
        }
    }

    private void addExpression() throws SyntaxException {
        multExpression();
        while (isKind(OP_PLUS) || isKind(OP_MINUS)) {
            match(t.kind);
            multExpression();
        }
    }

    private void multExpression() throws SyntaxException {
        powerExpression();
        while (isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)) {
            match(t.kind);
            powerExpression();
        }
    }

    private void powerExpression() throws SyntaxException {
        unaryExpression();
        if (isKind(OP_POWER)) {
            match(OP_POWER);
            powerExpression();
        }
    }

    private void unaryExpression() throws SyntaxException {
        switch (t.kind) {
            case OP_PLUS:
            case OP_MINUS:
            case OP_EXCLAMATION: {
                match(t.kind);
                unaryExpression();
            }
            break;
            default:
                primary();
        }
    }

    private void primary() throws SyntaxException {
        switch (t.kind) {
            case INTEGER_LITERAL:
            case BOOLEAN_LITERAL:
            case FLOAT_LITERAL: {
                match(t.kind);
            }
            break;
            case LPAREN: {
                match(LPAREN);
                expression();
                match(RPAREN);
            }
            break;
            case IDENTIFIER: {
                match(IDENTIFIER);
                if (isKind(LSQUARE)) {
                    pixelSelector();
                }
            }
            break;
            case LPIXEL: {
                pixelConstructor();
            }
            break;
            default: {
                if (isKind(functionName) | isKind(color)) {
                    match(t.kind);
                    if (isKind(LPAREN)) {
                        match(LPAREN);
                        expression();
                        match(RPAREN);
                    } else if (isKind(LSQUARE)) {
                        match(LSQUARE);
                        expression();
                        match(COMMA);
                        expression();
                        match(RSQUARE);
                    } else {
                        throw new SyntaxException(t, "Syntax Error: Wrong expression syntax for: " + t.getText()
                                + " at position: " + t.posInLine() + " in line: " + t.line());
                    }

                } else if (isKind(predefinedName)) {
                    match(t.kind);
                } else {
                    throw new SyntaxException(t, "Syntax Error: Wrong expression syntax for: " + t.getText()
                            + " at position: " + t.posInLine() + " in line: " + t.line());
                }
            }
        }
    }

    private void pixelConstructor() throws SyntaxException {
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

    private void pixelSelector() throws SyntaxException {
        match(LSQUARE);
        expression();
        match(COMMA);
        expression();
        match(RSQUARE);
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
        throw new SyntaxException(t, "Syntax Error: Wrong expression syntax/symbol for: " + t.getText()
                + " at position: " + t.posInLine() + " in line: " + t.line());
    }

    private Token consume() throws SyntaxException {
        Token tmp = t;
        if (isKind(EOF)) {
            throw new SyntaxException(t, "Syntax Error: Unexpected EOF at: " + t.posInLine() + " in line: " + t.line()); // TODO
            // give
            // a
            // better
            // error
            // message!
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
