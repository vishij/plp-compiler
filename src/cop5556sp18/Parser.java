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

import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;

import java.util.ArrayList;
import java.util.List;

import static cop5556sp18.Scanner.Kind.*;

public class Parser {

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

    Parser(Scanner scanner) {
        this.scanner = scanner;
        t = scanner.nextToken();
    }

    public Program parse() throws SyntaxException {
        Program node = program();
        matchEOF();
        return node;
    }

    /*
     * Program ::= Identifier Block
     */
    public Program program() throws SyntaxException {
        Token firstToken = t;
        Token progName = match(IDENTIFIER);
        Block block = block();
        return new Program(firstToken, progName, block);
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

    public Block block() throws SyntaxException {
        Token firstToken = t;
        List<ASTNode> decsOrStatements = new ArrayList<>();
        match(LBRACE);
        while (isKind(firstDec) | isKind(firstStatement) || isKind(color)) {
            if (isKind(firstDec)) {
                Declaration declaration = declaration();
                decsOrStatements.add(declaration);
            } else if (isKind(firstStatement) || isKind(color)) {
                Statement statement = statement();
                decsOrStatements.add(statement);
            }
            match(SEMI);
        }
        match(RBRACE);
        return new Block(firstToken, decsOrStatements);
    }

    public Declaration declaration() throws SyntaxException {
        Token firstToken = t;
        Token type = null;
        Token name = null;
        Expression width = null;
        Expression height = null;
        switch (t.kind) {
            case KW_image: {
                type = match(KW_image);
                name = match(IDENTIFIER);
                if (isKind(LSQUARE)) {
                    match(LSQUARE);
                    width = expression();
                    match(COMMA);
                    height = expression();
                    match(RSQUARE);
                }
            }
            break;
            case KW_int:
            case KW_float:
            case KW_boolean:
            case KW_filename: {
                type = match(t.kind);
                name = match(IDENTIFIER);
            }
            break;
            default:
                throw new SyntaxException(t,
                        "Wrong start of declaration. Should be int, float, boolean, image, or filename");
        }
        return new Declaration(firstToken, type, name, width, height);
    }

    public Statement statement() throws SyntaxException {
        Token firstToken = t;
        switch (t.kind) {
            case KW_input: {
                return statementInput();
            }
            case KW_write: {
                return statementWrite();
            }
            case KW_while: {
                return statementWhile();
            }
            case KW_if: {
                return statementIf();
            }
            case KW_show: {
                match(KW_show);
                Expression expr = expression();
                return new StatementShow(firstToken, expr);
            }
            case KW_sleep: {
                match(KW_sleep);
                Expression duration = expression();
                return new StatementSleep(firstToken, duration);
            }
            // TODO: refactor
            // LHS statement cases: LHS ::= IDENTIFIER | IDENTIFIER PixelSelector | Color (
            // IDENTIFIER PixelSelector )
            case IDENTIFIER:
            case KW_red:
            case KW_green:
            case KW_blue:
            case KW_alpha: {
                return statementAssignment();
            }
            default:
                throw new SyntaxException(t, "Syntax Error: Wrong statement syntax for: " + t.getText() + " at position: "
                        + t.posInLine() + " in line: " + t.line());
        }
    }

    // TODO: make statementIf and statementWhile methods generic
    private StatementIf statementIf() throws SyntaxException {
        Token firstToken = t;
        match(KW_if);
        match(LPAREN);
        Expression guard = expression();
        match(RPAREN);
        Block b = block();
        return new StatementIf(firstToken, guard, b);
    }

    private StatementWhile statementWhile() throws SyntaxException {
        Token firstToken = t;
        match(KW_while);
        match(LPAREN);
        Expression guard = expression();
        match(RPAREN);
        Block b = block();
        return new StatementWhile(firstToken, guard, b);
    }

    private StatementAssign statementAssignment() throws SyntaxException {
        Token firstToken = t;
        LHS lhs = lhs();
        match(OP_ASSIGN);
        Expression e = expression();
        return new StatementAssign(firstToken, lhs, e);
    }

    private LHS lhs() throws SyntaxException {
        Token firstToken = t;
        if (isKind(IDENTIFIER)) {
            Token name = match(IDENTIFIER);
            if (isKind(LSQUARE)) {
                PixelSelector pixelSelector = pixelSelector();
                return new LHSPixel(firstToken, name, pixelSelector);
            }
            return new LHSIdent(firstToken, name);
        } else if (isKind(color)) {
            Token color = match(t.kind);
            match(LPAREN);
            Token name = match(IDENTIFIER);
            PixelSelector pixelSelector = pixelSelector();
            match(RPAREN);
            return new LHSSample(firstToken, name, pixelSelector, color);
        } else {
            throw new SyntaxException(t, "Syntax Error: Wrong statement syntax for: " + t.getText() + " at position: "
                    + t.posInLine() + " in line: " + t.line());
        }
    }

    private StatementWrite statementWrite() throws SyntaxException {
        Token firstToken = t;
        match(KW_write);
        Token sourceName = match(IDENTIFIER);
        match(KW_to);
        Token destName = match(IDENTIFIER);
        return new StatementWrite(firstToken, sourceName, destName);
    }

    private StatementInput statementInput() throws SyntaxException {
        Token firstToken = t;
        match(KW_input);
        Token destName = match(IDENTIFIER);
        match(KW_from);
        match(OP_AT);
        Expression e = expression();
        return new StatementInput(firstToken, destName, e);
    }

    public Expression expression() throws SyntaxException {
        Token firstToken = t;
        Expression expr = orExpression();
        if (isKind(OP_QUESTION)) {
            match(OP_QUESTION);
            Expression trueExpr = expression();
            match(OP_COLON);
            Expression falseExpr = expression();
            expr = new ExpressionConditional(firstToken, expr, trueExpr, falseExpr);
        }
        return expr;
    }

    private Expression orExpression() throws SyntaxException {
        Token firstToken = t;
        Expression expr = andExpression();
        while (isKind(OP_OR)) {
            Token op = match(OP_OR);
            Expression rightExpr = andExpression();
            expr = new ExpressionBinary(firstToken, expr, op, rightExpr);
        }
        return expr;
    }

    private Expression andExpression() throws SyntaxException {
        Token firstToken = t;
        Expression expr = eqExpression();
        while (isKind(OP_AND)) {
            Token op = match(OP_AND);
            Expression rightExpr = eqExpression();
            expr = new ExpressionBinary(firstToken, expr, op, rightExpr);
        }
        return expr;
    }

    private Expression eqExpression() throws SyntaxException {
        Token firstToken = t;
        Expression expr = relExpression();
        while (isKind(OP_EQ) || isKind(OP_NEQ)) {
            Token op = match(t.kind);
            Expression rightExpr = relExpression();
            expr = new ExpressionBinary(firstToken, expr, op, rightExpr);
        }
        return expr;
    }

    private Expression relExpression() throws SyntaxException {
        Token firstToken = t;
        Expression expr = addExpression();
        while (isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE) || isKind(OP_GE)) {
            Token op = match(t.kind);
            Expression rightExpr = addExpression();
            expr = new ExpressionBinary(firstToken, expr, op, rightExpr);
        }
        return expr;
    }

    private Expression addExpression() throws SyntaxException {
        Token firstToken = t;
        Expression expr = multExpression();
        while (isKind(OP_PLUS) || isKind(OP_MINUS)) {
            Token op = match(t.kind);
            Expression rightExpr = multExpression();
            expr = new ExpressionBinary(firstToken, expr, op, rightExpr);
        }
        return expr;
    }

    private Expression multExpression() throws SyntaxException {
        Token firstToken = t;
        Expression expr = powerExpression();
        while (isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)) {
            Token op = match(t.kind);
            Expression rightExpr = powerExpression();
            expr = new ExpressionBinary(firstToken, expr, op, rightExpr);
        }
        return expr;
    }

    private Expression powerExpression() throws SyntaxException {
        Token firstToken = t;
        Expression expr = unaryExpression();
        if (isKind(OP_POWER)) {
            Token op = match(OP_POWER);
            Expression rightExpr = powerExpression();
            expr = new ExpressionBinary(firstToken, expr, op, rightExpr);

        }
        return expr;
    }

    private Expression unaryExpression() throws SyntaxException {
        Token firstToken = t;
        //TODO: switch to if else?
        switch (t.kind) {
            case OP_PLUS:
            case OP_MINUS:
            case OP_EXCLAMATION: {
                Token op = match(t.kind);
                Expression expression = unaryExpression();
                return new ExpressionUnary(firstToken, op, expression);
            } // below: UnaryExpressionNotPlusMinus and primary
            default:
                return primary();
        }
    }

    private Expression primary() throws SyntaxException {
        Token firstToken = t;
        switch (t.kind) {
            case INTEGER_LITERAL: {
                Token intLiteral = match(t.kind);
                return new ExpressionIntegerLiteral(firstToken, intLiteral);
            }
            case BOOLEAN_LITERAL: {
                Token boolLiteral = match(t.kind);
                return new ExpressionBooleanLiteral(firstToken, boolLiteral);
            }
            case FLOAT_LITERAL: {
                Token floatLit = match(t.kind);
                return new ExpressionFloatLiteral(firstToken, floatLit);
            }
            case LPAREN: {
                match(LPAREN);
                Expression expr = expression();
                match(RPAREN);
                return expr;
            }
            case IDENTIFIER: {
                Token name = match(IDENTIFIER);
                if (isKind(LSQUARE)) {
                    PixelSelector pixelSelector = pixelSelector();
                    return new ExpressionPixel(firstToken, name, pixelSelector);
                }
                return new ExpressionIdent(firstToken, name);
            }
            case LPIXEL: {
                return pixelConstructor();
            }
            default: {
                if (isKind(functionName) | isKind(color)) {
                    Token name = match(t.kind);
                    if (isKind(LPAREN)) {
                        match(LPAREN);
                        Expression e = expression();
                        match(RPAREN);
                        return new ExpressionFunctionAppWithExpressionArg(firstToken, name, e);
                    } else if (isKind(LSQUARE)) {
                        match(LSQUARE);
                        Expression e0 = expression();
                        match(COMMA);
                        Expression e1 = expression();
                        match(RSQUARE);
                        return new ExpressionFunctionAppWithPixel(firstToken, name, e0, e1);
                    } else {
                        throw new SyntaxException(t, "Syntax Error: Wrong expression syntax for: " + t.getText()
                                + " at position: " + t.posInLine() + " in line: " + t.line());
                    }

                } else if (isKind(predefinedName)) {
                    Token name = match(t.kind);
                    return new ExpressionPredefinedName(firstToken, name);
                } else {
                    throw new SyntaxException(t, "Syntax Error: Wrong expression syntax for: " + t.getText()
                            + " at position: " + t.posInLine() + " in line: " + t.line());
                }
            }
        }
    }

    private ExpressionPixelConstructor pixelConstructor() throws SyntaxException {
        Token firstToken = t;
        match(LPIXEL);
        Expression alpha = expression();
        match(COMMA);
        Expression red = expression();
        match(COMMA);
        Expression green = expression();
        match(COMMA);
        Expression blue = expression();
        match(RPIXEL);
        return new ExpressionPixelConstructor(firstToken, alpha, red, green, blue);
    }

    private PixelSelector pixelSelector() throws SyntaxException {
        Token firstToken = t;
        match(LSQUARE);
        Expression ex = expression();
        match(COMMA);
        Expression ey = expression();
        match(RSQUARE);
        return new PixelSelector(firstToken, ex, ey);
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
