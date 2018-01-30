/**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles
 * at the University of Florida, Spring 2018.
 * <p>
 * This software is solely for the educational benefit of students
 * enrolled in the course during the Spring 2018 semester.
 * <p>
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 *
 * @Beverly A. Sanders, 2018
 */

package cop5556sp18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;

import static cop5556sp18.Scanner.Kind.*;

public class ScannerTest {

    //set Junit to be able to catch exceptions
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    //To make it easy to print objects and turn this output on and off
    static boolean doPrint = true;

    private void show(Object input) {
        if (doPrint) {
            System.out.println(input.toString());
        }
    }

    /**
     * Retrieves the next token and checks that it is an EOF token.
     * Also checks that this was the last token.
     *
     * @param scanner
     * @return the Token that was retrieved
     */

    Token checkNextIsEOF(Scanner scanner) {
        Scanner.Token token = scanner.nextToken();
        assertEquals(Scanner.Kind.EOF, token.kind);
        assertFalse(scanner.hasTokens());
        return token;
    }


    /**
     * Retrieves the next token and checks that its kind, position, length, line, and position in line
     * match the given parameters.
     *
     * @param scanner
     * @param kind
     * @param pos
     * @param length
     * @param line
     * @param pos_in_line
     * @return the Token that was retrieved
     */
    Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
        Token t = scanner.nextToken();
        assertEquals(kind, t.kind);
        assertEquals(pos, t.pos);
        assertEquals(length, t.length);
        assertEquals(line, t.line());
        assertEquals(pos_in_line, t.posInLine());
        return t;
    }

    /**
     * Retrieves the next token and checks that its kind and length match the given
     * parameters.  The position, line, and position in line are ignored.
     *
     * @param scanner
     * @param kind
     * @param length
     * @return the Token that was retrieved
     */
    Token checkNext(Scanner scanner, Scanner.Kind kind, int length) {
        Token t = scanner.nextToken();
        assertEquals(kind, t.kind);
        assertEquals(length, t.length);
        return t;
    }


    /**
     * Simple test case with an empty program.  The only Token will be the EOF Token.
     *
     * @throws LexicalException
     */
    @Test
    public void testEmpty() throws LexicalException {
        String input = "";  //The input is the empty string.  This is legal
        show(input);        //Display the input
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        show(scanner);   //Display the Scanner
        checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
    }

    /**
     * Test illustrating how to put a new line in the input program and how to
     * check content of tokens.
     * <p>
     * Because we are using a Java String literal for input, we use \n for the
     * end of line character. (We should also be able to handle \n, \r, and \r\n
     * properly.)
     * <p>
     * Note that if we were reading the input from a file, the end of line
     * character would be inserted by the text editor.
     * Showing the input will let you check your input is
     * what you think it is.
     *
     * @throws LexicalException
     */
    @Test
    public void testSemi() throws LexicalException {
        String input = ";;\n;;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, SEMI, 1, 1, 1, 2);
        checkNext(scanner, SEMI, 3, 1, 2, 1);
        checkNext(scanner, SEMI, 4, 1, 2, 2);
        checkNextIsEOF(scanner);
    }


    /**
     * This example shows how to test that your scanner is behaving when the
     * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
     * <p>
     * The example shows catching the exception that is thrown by the scanner,
     * looking at it, and checking its contents before rethrowing it.  If caught
     * but not rethrown, then JUnit won't get the exception and the test will fail.
     * <p>
     * The test will work without putting the try-catch block around
     * new Scanner(input).scan(); but then you won't be able to check
     * or display the thrown exception.
     *
     * @throws LexicalException
     */
    @Test
    public void failIllegalChar() throws LexicalException {
        String input = ";;~";
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {  //Catch the exception
            show(e);                    //Display it
            assertEquals(2, e.getPos()); //Check that it occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }


    @Test
    public void testParens() throws LexicalException {
        String input = "()";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, LPAREN, 0, 1, 1, 1);
        checkNext(scanner, RPAREN, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }
    
    /* ----------------- NEW TEST CASES START --------------------- */

    @Test
    public void testSpace() throws LexicalException {
        String input = " ";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
//        checkNext(scanner, LPAREN, 0, 1, 1, 1);
//        checkNext(scanner, RPAREN, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testLessThan() throws LexicalException {
        String input = ";<;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, OP_LT, 1, 1, 1, 2);
        checkNext(scanner, SEMI, 2, 1, 1, 3);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testLessThanEq() throws LexicalException {
        String input = ";<=;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, OP_LE, 1, 2, 1, 2);
        checkNext(scanner, SEMI, 3, 1, 1, 4);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testLeftPixel() throws LexicalException {
        String input = ";<<;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, LPIXEL, 1, 2, 1, 2);
        checkNext(scanner, SEMI, 3, 1, 1, 4);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testGreaterThan() throws LexicalException {
        String input = ";>;>";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, OP_GT, 1, 1, 1, 2);
        checkNext(scanner, SEMI, 2, 1, 1, 3);
        checkNext(scanner, OP_GT, 3, 1, 1, 4);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testGreaterThanEq() throws LexicalException {
        String input = ";>=;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, OP_GE, 1, 2, 1, 2);
        checkNext(scanner, SEMI, 3, 1, 1, 4);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testRightPixel() throws LexicalException {
        String input = ";>>;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, RPIXEL, 1, 2, 1, 2);
        checkNext(scanner, SEMI, 3, 1, 1, 4);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testNextLineAtStart() throws LexicalException {
        String input = "\n;<;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 1, 1, 2, 1);
        checkNext(scanner, OP_LT, 2, 1, 2, 2);
        checkNext(scanner, SEMI, 3, 1, 2, 3);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testColon() throws LexicalException {
        String input = ";::;:";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, OP_COLON, 1, 1, 1, 2);
        checkNext(scanner, OP_COLON, 2, 1, 1, 3);
        checkNext(scanner, SEMI, 3, 1, 1, 4);
        checkNext(scanner, OP_COLON, 4, 1, 1, 5);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testAssign() throws LexicalException {
        String input = ";:=;:";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, OP_ASSIGN, 1, 2, 1, 2);
        checkNext(scanner, SEMI, 3, 1, 1, 4);
        checkNext(scanner, OP_COLON, 4, 1, 1, 5);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testEqual() throws LexicalException {
        String input = ";==;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, OP_EQ, 1, 2, 1, 2);
        checkNext(scanner, SEMI, 3, 1, 1, 4);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testFailSingleEqualChar() throws LexicalException {
        String input = ";=:;:";
//        Scanner scanner = new Scanner(input).scan();
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(1, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testExclamation() throws LexicalException {
        String input = ";!:;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, OP_EXCLAMATION, 1, 1, 1, 2);
        checkNext(scanner, OP_COLON, 2, 1, 1, 3);
        checkNext(scanner, SEMI, 3, 1, 1, 4);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testNotEqual() throws LexicalException {
        String input = ";!=;:";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, OP_NEQ, 1, 2, 1, 2);
        checkNext(scanner, SEMI, 3, 1, 1, 4);
        checkNext(scanner, OP_COLON, 4, 1, 1, 5);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testEqNotEqual() throws LexicalException {
        String input = ";!===;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);

        checkNext(scanner, SEMI, 0, 1, 1, 1);
        checkNext(scanner, OP_NEQ, 1, 2, 1, 2);
        checkNext(scanner, OP_EQ, 3, 2, 1, 4);
        checkNext(scanner, SEMI, 5, 1, 1, 6);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testBraces() throws LexicalException {
        String input = "{}";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, LBRACE, 0, 1, 1, 1);
        checkNext(scanner, RBRACE, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testSquareBraces() throws LexicalException {
        String input = "[]";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, LSQUARE, 0, 1, 1, 1);
        checkNext(scanner, RSQUARE, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testComma() throws LexicalException {
        String input = ",;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, COMMA, 0, 1, 1, 1);
        checkNext(scanner, SEMI, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testQuestion() throws LexicalException {
        String input = "?;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_QUESTION, 0, 1, 1, 1);
        checkNext(scanner, SEMI, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testAnd() throws LexicalException {
        String input = "&;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_AND, 0, 1, 1, 1);
        checkNext(scanner, SEMI, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testOr() throws LexicalException {
        String input = "|;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_OR, 0, 1, 1, 1);
        checkNext(scanner, SEMI, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testPlus() throws LexicalException {
        String input = "+;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_PLUS, 0, 1, 1, 1);
        checkNext(scanner, SEMI, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testMinus() throws LexicalException {
        String input = "-;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_MINUS, 0, 1, 1, 1);
        checkNext(scanner, SEMI, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testMod() throws LexicalException {
        String input = "%;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_MOD, 0, 1, 1, 1);
        checkNext(scanner, SEMI, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testAt() throws LexicalException {
        String input = "@;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_AT, 0, 1, 1, 1);
        checkNext(scanner, SEMI, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testZero() throws LexicalException {
        String input = "0;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, SEMI, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testNonZero() throws LexicalException {
        String input = "1234;";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 4, 1, 1);
        checkNext(scanner, SEMI, 4, 1, 1, 5);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testDigits() throws LexicalException {
        String input = "01230";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, INTEGER_LITERAL, 1, 4, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testIntegerLiteralOutOfRange() throws LexicalException {
        String input = "4294967296";
        show(input);
        thrown.expect(LexicalException.class);
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(0, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testMultipleZeros() throws LexicalException {
        String input = "00";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, INTEGER_LITERAL, 1, 1, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testSingleAlphabet() throws LexicalException {
        String input = "a";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, IDENTIFIER, 0, 1, 1, 1);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testAlphabets() throws LexicalException {
        String input = "meh meh booo0";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);
        checkNext(scanner, IDENTIFIER, 4, 3, 1, 5);
        checkNext(scanner, IDENTIFIER, 8, 5, 1, 9);
        checkNextIsEOF(scanner);
    }


    @Test
    public void testInvalidIdentifier() throws LexicalException {
        String input = "$abc";
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(0, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testInvalidIdentifierStart() throws LexicalException {
        String input = "_123";
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(0, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testInvalidIdentifierStartDollar() throws LexicalException {
        String input = "$123";
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(0, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testInvalidIdentifierUnderscore() throws LexicalException {
        String input = "123_";
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(3, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testInvalidIdentifierDollar() throws LexicalException {
        String input = "123$";
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(3, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testInvalidIdentUnderscoreMid() throws LexicalException {
        String input = "123_a";
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(3, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testInvalidIdentDollarMid() throws LexicalException {
        String input = "123$a";
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(3, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testValidIdentifier() throws LexicalException {
        String input = "meh_meh booo0";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, IDENTIFIER, 0, 7, 1, 1);
        checkNext(scanner, IDENTIFIER, 8, 5, 1, 9);
//        checkNext(scanner, INTEGER_LITERAL, 12, 1, 1, 13);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testValidIdentifierDollar() throws LexicalException {
        String input = "meh$meh booo0";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, IDENTIFIER, 0, 7, 1, 1);
        checkNext(scanner, IDENTIFIER, 8, 5, 1, 9);
//        checkNext(scanner, INTEGER_LITERAL, 12, 1, 1, 13);
        checkNextIsEOF(scanner);
    }


    @Test
    public void testLiteralIdentifier() throws LexicalException {
        String input = "123booo0";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
        checkNext(scanner, IDENTIFIER, 3, 5, 1, 4);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testIdentifier() throws LexicalException {
        String input = "booo01256ab";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, IDENTIFIER, 0, 11, 1, 1);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testIdentifierLiteral() throws LexicalException {
        String input = "0123booo";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, INTEGER_LITERAL, 1, 3, 1, 2);
        checkNext(scanner, IDENTIFIER, 4, 4, 1, 5);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testKeyword() throws LexicalException {
        String input = "0123while";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, INTEGER_LITERAL, 1, 3, 1, 2);
        checkNext(scanner, KW_while, 4, 5, 1, 5);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testKeywordIdent() throws LexicalException {
        String input = "0123whiletrue";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, INTEGER_LITERAL, 1, 3, 1, 2);
        checkNext(scanner, IDENTIFIER, 4, 9, 1, 5);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testBooleanLiteral() throws LexicalException {
        String input = "0123true";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, INTEGER_LITERAL, 1, 3, 1, 2);
        checkNext(scanner, BOOLEAN_LITERAL, 4, 4, 1, 5);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testBooleanIdent() throws LexicalException {
        String input = "0123trueif";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, INTEGER_LITERAL, 1, 3, 1, 2);
        checkNext(scanner, IDENTIFIER, 4, 6, 1, 5);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testDot() throws LexicalException {
        String input = ".";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, DOT, 0, 1, 1, 1);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testFloatZeroBefore() throws LexicalException {
        String input = "0.";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, FLOAT_LITERAL, 0, 2, 1, 1);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testFloatZeroAfter() throws LexicalException {
        String input = ".0";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, FLOAT_LITERAL, 0, 2, 1, 1);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testFloatZero() throws LexicalException {
        String input = "0.0";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, FLOAT_LITERAL, 0, 3, 1, 1);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testFloatNonZeroBefore() throws LexicalException {
        String input = "8.";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, FLOAT_LITERAL, 0, 2, 1, 1);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testFloatNonZeroAfter() throws LexicalException {
        String input = ".8";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, FLOAT_LITERAL, 0, 2, 1, 1);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testFloatNonZero() throws LexicalException {
        String input = "19.0";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, FLOAT_LITERAL, 0, 4, 1, 1);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testLiteralAndNonZeroFloat() throws LexicalException {
        String input = "01.23";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, FLOAT_LITERAL, 1, 4, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testLiteralZeroFloat() throws LexicalException {
        String input = "00.";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, FLOAT_LITERAL, 1, 2, 1, 2);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testLiteralFloat() throws LexicalException {
        String input = "01.";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
        checkNext(scanner, FLOAT_LITERAL, 1, 2, 1, 2);
        checkNextIsEOF(scanner);
    }
    
    @Test
    public void testFloatDot() throws LexicalException {
        String input = ".333.";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, FLOAT_LITERAL, 0, 4, 1, 1);
        checkNext(scanner, DOT, 4, 1, 1, 5);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testFloatOutOfRange() throws LexicalException {
        String input = "999999999999999999999999999999999999999999999999999.999990000";
        show(input);
        thrown.expect(LexicalException.class);
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(0, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testTimesPower() throws LexicalException {
        String input = "***";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_POWER, 0, 2, 1, 1);
        checkNext(scanner, OP_TIMES, 2, 1, 1, 3);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testTimesDiv() throws LexicalException {
        String input = "***/";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_POWER, 0, 2, 1, 1);
        checkNext(scanner, OP_TIMES, 2, 1, 1, 3);
        checkNext(scanner, OP_DIV, 3, 1, 1, 4);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testEmptyComment() throws LexicalException {
        String input = "/**/";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testInvalidComment() throws LexicalException {
        String input = "/*/*";
        show(input);
        thrown.expect(LexicalException.class);
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(0, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }

    @Test
    public void testValidComment() throws LexicalException {
        String input = "/*ABC*def/****/";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testCommentWithStar() throws LexicalException {
        String input = "/***/";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testCommentWithNextLine() throws LexicalException {
        String input = "/**\n*/*abc";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_TIMES, 6, 1, 2, 3);
        checkNext(scanner, IDENTIFIER, 7, 3, 2, 4);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testNestedComment() throws LexicalException {
        String input = "/*/**/*/";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_TIMES, 6, 1, 1, 7);
        checkNext(scanner, OP_DIV, 7, 1, 1, 8);
        checkNextIsEOF(scanner);
    }

    @Test
    public void testNestedComment2() throws LexicalException {
        String input = "/**/**/*/";
        show(input);
        thrown.expect(LexicalException.class);
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {
            show(e);
            assertEquals(6, e.getPos()); //Check that exception occurred in the expected position
            throw e;                    //Rethrow exception so JUnit will see it
        }
    }
}


