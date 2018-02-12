/**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles
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

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.SimpleParser.SyntaxException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class SimpleParserTest {

    // set Junit to be able to catch exceptions
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // To make it easy to print objects and turn this output on and off
    static final boolean doPrint = true;

    private void show(Object input) {
        if (doPrint) {
            System.out.println(input.toString());
        }
    }

    // creates and returns a parser for the given input.
    private SimpleParser makeParser(String input) throws LexicalException {
        show(input); // Display the input
        Scanner scanner = new Scanner(input).scan(); // Create a Scanner and initialize it
        show(scanner); // Display the Scanner
        SimpleParser parser = new SimpleParser(scanner);
        return parser;
    }

    /**
     * Simple test case with an empty program. This throws an exception because it
     * lacks an identifier and a block. The test case passes because it expects an
     * exception
     *
     * @throws LexicalException
     * @throws SyntaxException
     */
    @Test
    public void testEmpty() throws LexicalException, SyntaxException {
        String input = ""; // The input is the empty string.
        SimpleParser parser = makeParser(input);
        thrown.expect(SyntaxException.class);
        parser.parse();
    }

    /**
     * Smallest legal program.
     *
     * @throws LexicalException
     * @throws SyntaxException
     */
    @Test
    public void testSmallest() throws LexicalException, SyntaxException {
        String input = "b{}";
        SimpleParser parser = makeParser(input);
        parser.parse();
    }

    // This test should pass in your complete parser. It will fail in the starter
    // code.
    // Of course, you would want a better error message.
    @Test
    public void testDec0() throws LexicalException, SyntaxException {
        String input = "b{int c;}";
        SimpleParser parser = makeParser(input);
        parser.parse();
    }

    @Test
    public void testNoSemi() throws LexicalException, SyntaxException {
        String input = "b{int c}";
        thrown.expect(SyntaxException.class);
        try {
            SimpleParser parser = makeParser(input);
            parser.parse();
        } catch (SyntaxException e) {
            show(e);
            assertEquals("Syntax Error: Wrong expression syntax/symbol for: } at position: 8 in line: 1", e.getMessage());                                                                                // expected position
            throw e;
        }
    }

    @Test
    public void testDemo1() throws LexicalException, SyntaxException {
        String input = "demo1 { \n" + "image h; \n" + "input h from @0; \n" + "show h; \n" + "sleep(4000); \n"
                + "image g[width(h),height(h)]; \n" + "int x; \n" + "x:=0; \n" + "while(x<width(g)){int y; \n"
                + "y:=0; \n" + "while(y<height(g)){g[x,y]:=h[y,x]; \n" + "y:=y+1; \n" + "}; \n" + "x:=x+1; \n" + "}; \n"
                + "show g; \n" + "sleep(4000); \n" + "}";
        SimpleParser parser = makeParser(input);
        parser.parse();
    }

    @Test
    public void makeRedImage() throws LexicalException, SyntaxException {
        String input = "makeRedImage { \n" + "image im[256,256]; \n" + "int x; \n" + "int y; \n" + "x := 0; \n"
                + "y := 0; \n" + "while (x < width(im)) { \n" + "y := 0; \n" + "while (y < height(im)) { \n"
                + "im[x,y] := <<255,255,0,0>>; \n" + "y := y+1; \n" + "}; \n" + "x := x+1; \n" + "}; \n" + "show im; \n"
                + "}";
        SimpleParser parser = makeParser(input);
        parser.parse();
    }

    @Test
    public void testPolarR2() throws LexicalException, SyntaxException {
        String input = "PolarR2 { \n" + "image im[1024,1024]; \n" + "int x; \n" + "x := 0; \n"
                + "while (x < width(im)) { \n" + "int y; \n" + "y := 0; \n" + "while (y < height(im)) { \n"
                + "float p; \n" + "p := polar_r[x,y]; \n" + "int r; \n" + "r := int(p) % Z; \n"
                + "im[x,y] := <<Z, 0, 0, r>>; \n" + "y := y+1; \n" + "}; \n" + "x := x + 1; \n" + "}; \n"
                + "show im; \n" + "}";
        SimpleParser parser = makeParser(input);
        parser.parse();
    }

    @Test
    public void testBirdSamples() throws LexicalException, SyntaxException {
        String input = "samples { \n" + "image bird; \n" + "input bird from @0; \n" + "show bird; \n"
                + "sleep(4000); \n" + "image bird2[width(bird),height(bird)]; \n" + "int x; \n" + "x:=0; \n"
                + "while(x<width(bird2)) {int y; \n" + "y:=0; \n"
                + "while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]); \n"
                + "green(bird2[x,y]):=blue(bird[x,y]); \n" + "red(bird2[x,y]):=green(bird[x,y]); \n"
                + "alpha(bird2[x,y]):=Z; \n" + "y:=y+1; \n" + "}; \n" + "x:=x+       1; \n" + "}; \n" + "show bird2; \n"
                + "sleep(4000); \n" + "}";
        SimpleParser parser = makeParser(input);
        parser.parse();
    }

    @Test
    public void testDeclaration() throws LexicalException, SyntaxException {
        String input = "ident { image boo; }";
        SimpleParser parser = makeParser(input);
        parser.parse();
    }

    @Test
    public void testDeclarationError() throws LexicalException, SyntaxException {
        String input = "ident { image; }";
        thrown.expect(SyntaxException.class);
        try {
            SimpleParser parser = makeParser(input);
            parser.parse();
        } catch (SyntaxException e) {
            show(e);
            assertEquals("Syntax Error: Wrong expression syntax/symbol for: ; at position: 14 in line: 1", e.getMessage());                                                                                // expected position
            throw e;
        }
    }

    @Test
    public void testDeclarations() throws LexicalException, SyntaxException {
        String input = "ident { image boo[a,b]; image meh[1,2]; image foo[a|b,c&d]; int lol; float blah; boolean moo; filename file; }";
        SimpleParser parser = makeParser(input);
        parser.parse();
    }

    @Test
    public void testImageEmptyExpressionError() throws LexicalException, SyntaxException {
        String input = "ident { image meh[]; }";
        thrown.expect(SyntaxException.class);
        try {
            SimpleParser parser = makeParser(input);
            parser.parse();
        } catch (SyntaxException e) {
            show(e);
            assertEquals("Syntax Error: Wrong expression syntax for: ] at position: 19 in line: 1", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testAssignment() throws LexicalException, SyntaxException {
        String input = "prog {int y; \n y:=0;}";
        SimpleParser parser = makeParser(input);
        parser.parse();
    }

    @Test
    public void testWrongAssignment() throws LexicalException, SyntaxException {
        String input = "prog {int y; \n y:=;}";
        thrown.expect(SyntaxException.class);
        try {
            SimpleParser parser = makeParser(input);
            parser.parse();
        } catch (SyntaxException e) {
            show(e);
            assertEquals("Syntax Error: Wrong expression syntax for: ; at position: 5 in line: 2", e.getMessage());
            throw e;
        }
    }
}
