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

import cop5556sp18.AST.*;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.LexicalException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static cop5556sp18.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

public class ParserTest {

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
    private Parser makeParser(String input) throws LexicalException {
        show(input); // Display the input
        Scanner scanner = new Scanner(input).scan(); // Create a Scanner and initialize it
        show(scanner); // Display the Scanner
        Parser parser = new Parser(scanner);
        return parser;
    }

    /**
     * Simple test case with an empty program. This throws an exception because it
     * lacks an identifier and a block
     *
     * @throws LexicalException
     * @throws SyntaxException
     */
    @Test
    public void testEmpty() throws LexicalException, SyntaxException {
        String input = ""; // The input is the empty string.
        thrown.expect(SyntaxException.class);
        Parser parser = makeParser(input);
        @SuppressWarnings("unused")
        Program p = parser.parse();
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
        Parser parser = makeParser(input);
        Program p = parser.parse();
        show(p);
        assertEquals("b", p.progName);
        assertEquals(0, p.block.decsOrStatements.size());
    }

    /**
     * Checks that an element in a block is a declaration with the given type and
     * name. The element to check is indicated by the value of index.
     *
     * @param block
     * @param index
     * @param type
     * @param name
     * @return
     */
    Declaration checkDec(Block block, int index, Kind type, String name) {
        ASTNode node = block.decOrStatement(index);
        assertEquals(Declaration.class, node.getClass());
        Declaration dec = (Declaration) node;
        assertEquals(type, dec.type);
        assertEquals(name, dec.name);
        return dec;
    }

    @Test
    public void testDec0() throws LexicalException, SyntaxException {
        String input = "b{int c; image j;}";
        Parser parser = makeParser(input);
        Program p = parser.parse();
        show(p);
        checkDec(p.block, 0, Kind.KW_int, "c");
        checkDec(p.block, 1, Kind.KW_image, "j");
    }

    /**
     * This test illustrates how you can test specific grammar elements by
     * themselves by calling the corresponding parser method directly, instead of
     * calling parse. This requires that the methods are visible (not private).
     *
     * @throws LexicalException
     * @throws SyntaxException
     */

    @Test
    public void testExpression() throws LexicalException, SyntaxException {
        String input = "x + 2";
        Parser parser = makeParser(input);
        Expression e = parser.expression(); // call expression here instead of parse
        show(e);
        assertEquals(ExpressionBinary.class, e.getClass());
        ExpressionBinary b = (ExpressionBinary) e;
        assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
        ExpressionIdent left = (ExpressionIdent) b.leftExpression;
        assertEquals("x", left.name);
        assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
        ExpressionIntegerLiteral right = (ExpressionIntegerLiteral) b.rightExpression;
        assertEquals(2, right.value);
        assertEquals(OP_PLUS, b.op);
    }

    @Test
    public void testPowerExpression() throws LexicalException, SyntaxException {
        String input = "x ** + 2";
        Parser parser = makeParser(input);
        Expression e = parser.expression();
        show(e);
        assertEquals(ExpressionBinary.class, e.getClass());
        ExpressionBinary b = (ExpressionBinary) e;
        assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
        ExpressionIdent left = (ExpressionIdent) b.leftExpression;
        assertEquals("x", left.name);
        assertEquals(ExpressionUnary.class, b.rightExpression.getClass());
        ExpressionUnary right = (ExpressionUnary) b.rightExpression;
        assertEquals(ExpressionIntegerLiteral.class, right.expression.getClass());
        ExpressionIntegerLiteral intLit = (ExpressionIntegerLiteral) right.expression;
        assertEquals(2, intLit.value);
        assertEquals(OP_PLUS, right.op);
        assertEquals(OP_POWER, b.op);
    }


    @Test
    public void testAddMultExpression() throws LexicalException, SyntaxException {
        String input = "12 + a * 2";
        Parser parser = makeParser(input);
        Expression e = parser.expression();
        show(e);
        assertEquals(ExpressionBinary.class, e.getClass());
        ExpressionBinary b = (ExpressionBinary) e;
        assertEquals(ExpressionIntegerLiteral.class, b.leftExpression.getClass());
        ExpressionIntegerLiteral left = (ExpressionIntegerLiteral) b.leftExpression;
        assertEquals(12, left.value);
        assertEquals(ExpressionBinary.class, b.rightExpression.getClass());
        ExpressionBinary right = (ExpressionBinary) b.rightExpression;
        assertEquals(ExpressionIdent.class, right.leftExpression.getClass());
        ExpressionIdent ident = (ExpressionIdent) right.leftExpression;
        assertEquals("a", ident.name);
        assertEquals(ExpressionIntegerLiteral.class, right.rightExpression.getClass());
        ExpressionIntegerLiteral intLit = (ExpressionIntegerLiteral) right.rightExpression;
        assertEquals(2, intLit.value);
        assertEquals(OP_PLUS, b.op);
        assertEquals(OP_TIMES, right.op);
    }

    @Test
    public void testMultDivExpression() throws LexicalException, SyntaxException {
        String input = "12 * a / 2";
        Parser parser = makeParser(input);
        Expression e = parser.expression();
        show(e);
        assertEquals(ExpressionBinary.class, e.getClass());
        ExpressionBinary b = (ExpressionBinary) e;
        assertEquals(ExpressionBinary.class, b.leftExpression.getClass());
        ExpressionBinary left = (ExpressionBinary) b.leftExpression;
        assertEquals(ExpressionIntegerLiteral.class, left.leftExpression.getClass());
        ExpressionIntegerLiteral intLit = (ExpressionIntegerLiteral) left.leftExpression;
        assertEquals(12, intLit.value);
        assertEquals(OP_TIMES, left.op);
        assertEquals(ExpressionIdent.class, left.rightExpression.getClass());
        ExpressionIdent right = (ExpressionIdent) left.rightExpression;
        assertEquals("a", right.name);
        assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
        ExpressionIntegerLiteral rightEx = (ExpressionIntegerLiteral) b.rightExpression;
        assertEquals(2, rightEx.value);
        assertEquals(OP_DIV, b.op);
    }

    @Test
    public void testPowerAssociativityExpression() throws LexicalException, SyntaxException {
        String input = "12 ** a ** 2";
        Parser parser = makeParser(input);
        Expression e = parser.expression();
        show(e);
        assertEquals(ExpressionBinary.class, e.getClass());
        ExpressionBinary b = (ExpressionBinary) e;
        assertEquals(ExpressionIntegerLiteral.class, b.leftExpression.getClass());
        ExpressionIntegerLiteral left = (ExpressionIntegerLiteral) b.leftExpression;
        assertEquals(12, left.value);
        assertEquals(OP_POWER, b.op);
        assertEquals(ExpressionBinary.class, b.rightExpression.getClass());
        ExpressionBinary right = (ExpressionBinary) b.rightExpression;
        assertEquals(ExpressionIdent.class, right.leftExpression.getClass());
        ExpressionIdent leftEx = (ExpressionIdent) right.leftExpression;
        assertEquals("a", leftEx.name);
        assertEquals(OP_POWER, right.op);
        assertEquals(ExpressionIntegerLiteral.class, right.rightExpression.getClass());
        ExpressionIntegerLiteral rightEx = (ExpressionIntegerLiteral) right.rightExpression;
        assertEquals(2, rightEx.value);
    }

    @Test
    public void testDemo1() throws LexicalException, SyntaxException {
        String input = "demo1 { \n" + "image h; \n" + "input h from @0; \n" + "show h; \n" + "sleep(4000); \n"
                + "image g[width(h),height(h)]; \n" + "int x; \n" + "x:=0; \n" + "while(x<width(g)){int y; \n"
                + "y:=0; \n" + "while(y<height(g)){g[x,y]:=h[y,x]; \n" + "y:=y+1; \n" + "}; \n" + "x:=x+1; \n" + "}; \n"
                + "show g; \n" + "sleep(4000); \n" + "}";
        Parser parser = makeParser(input);
        Program p = parser.parse();
        show(p);
        assertEquals(p.toString(),
                "Program [progName=demo1, block=Block [decsOrStatements=[Declaration [type=KW_image, name=h, width=null, height=null], StatementInput [destName=h, e=ExpressionIntegerLiteral [value=0]], ShowStatement [e=ExpressionIdent [name=h]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]], Declaration [type=KW_image, name=g, width=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=h]], height=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=h]]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=g]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=g]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSPixel [name=g, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixel [name=h, pixelSelector=PixelSelector [ex=ExpressionIdent [name=y], ey=ExpressionIdent [name=x]]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=g]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]]]]]");
    }

    @Test
    public void makeRedImage() throws LexicalException, SyntaxException {
        String input = "makeRedImage { \n" + "image im[256,256]; \n" + "int x; \n" + "int y; \n" + "x := 0; \n"
                + "y := 0; \n" + "while (x < width(im)) { \n" + "y := 0; \n" + "while (y < height(im)) { \n"
                + "im[x,y] := <<255,255,0,0>>; \n" + "y := y+1; \n" + "}; \n" + "x := x+1; \n" + "}; \n" + "show im; \n"
                + "}";
        Parser parser = makeParser(input);
        Program p = parser.parse();
        show(p);
        assertEquals(p.toString(),
                "Program [progName=makeRedImage, block=Block [decsOrStatements=[Declaration [type=KW_image, name=im, width=ExpressionIntegerLiteral [value=256], height=ExpressionIntegerLiteral [value=256]], Declaration [type=KW_int, name=x, width=null, height=null], Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSPixel [name=im, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixelConstructor [alpha=ExpressionIntegerLiteral [value=255], red=ExpressionIntegerLiteral [value=255], green=ExpressionIntegerLiteral [value=0], blue=ExpressionIntegerLiteral [value=0]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=im]]]]]");
    }

    @Test
    public void testPolarR2() throws LexicalException, SyntaxException {
        String input = "PolarR2 { \n" + "image im[1024,1024]; \n" + "int x; \n" + "x := 0; \n"
                + "while (x < width(im)) { \n" + "int y; \n" + "y := 0; \n" + "while (y < height(im)) { \n"
                + "float p; \n" + "p := polar_r[x,y]; \n" + "int r; \n" + "r := int(p) % Z; \n"
                + "im[x,y] := <<Z, 0, 0, r>>; \n" + "y := y+1; \n" + "}; \n" + "x := x + 1; \n" + "}; \n"
                + "show im; \n" + "}";
        Parser parser = makeParser(input);
        Program p = parser.parse();
        show(p);
        assertEquals(p.toString(),
                "Program [progName=PolarR2, block=Block [decsOrStatements=[Declaration [type=KW_image, name=im, width=ExpressionIntegerLiteral [value=1024], height=ExpressionIntegerLiteral [value=1024]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[Declaration [type=KW_float, name=p, width=null, height=null], StatementAssign [lhs=LHSIdent [name=p], e=ExpressionFunctionAppWithPixel [name=KW_polar_r, e0=ExpressionIdent [name=x], e1=ExpressionIdent [name=y]]], Declaration [type=KW_int, name=r, width=null, height=null], StatementAssign [lhs=LHSIdent [name=r], e=ExpressionBinary [leftExpression=ExpressionFunctionApp [function=KW_int, e=ExpressionIdent [name=p]], op=OP_MOD, rightExpression=ExpressionPredefinedName [name=KW_Z]]], StatementAssign [lhs=LHSPixel [name=im, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixelConstructor [alpha=ExpressionPredefinedName [name=KW_Z], red=ExpressionIntegerLiteral [value=0], green=ExpressionIntegerLiteral [value=0], blue=ExpressionIdent [name=r]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=im]]]]]");
    }

    @Test
    public void testSamples() throws LexicalException, SyntaxException {
        String input = "samples { \n" + "image bird; \n" + "input bird from @0; \n" + "show bird; \n"
                + "sleep(4000); \n" + "image bird2[width(bird),height(bird)]; \n" + "int x; \n" + "x:=0; \n"
                + "while(x<width(bird2)) {int y; \n" + "y:=0; \n"
                + "while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]); \n"
                + "green(bird2[x,y]):=blue(bird[x,y]); \n" + "red(bird2[x,y]):=green(bird[x,y]); \n"
                + "alpha(bird2[x,y]):=Z; \n" + "y:=y+1; \n" + "}; \n" + "x:=x+       1; \n" + "}; \n" + "show bird2; \n"
                + "sleep(4000); \n" + "}";
        Parser parser = makeParser(input);
        Program p = parser.parse();
        show(p);
        assertEquals(p.toString(),
                "Program [progName=samples, block=Block [decsOrStatements=[Declaration [type=KW_image, name=bird, width=null, height=null], StatementInput [destName=bird, e=ExpressionIntegerLiteral [value=0]], ShowStatement [e=ExpressionIdent [name=bird]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]], Declaration [type=KW_image, name=bird2, width=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=bird]], height=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=bird]]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=bird2]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=bird2]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_blue], e=ExpressionFunctionApp [function=KW_red, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_green], e=ExpressionFunctionApp [function=KW_blue, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_red], e=ExpressionFunctionApp [function=KW_green, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_alpha], e=ExpressionPredefinedName [name=KW_Z]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=bird2]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]]]]]");
    }

    @Test
    public void testPolar() throws LexicalException, SyntaxException {
        String input = "Polar { \n" + "p := polar_r[x,y]; \n" + "}";
        Parser parser = makeParser(input);
        Program p = parser.parse();
        show(p);
        assertEquals(p.toString(), "Program [progName=Polar, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=p], e=ExpressionFunctionAppWithPixel [name=KW_polar_r, e0=ExpressionIdent [name=x], e1=ExpressionIdent [name=y]]]]]]");

    }

    @Test
    public void testDeclarationError() throws LexicalException, SyntaxException {
        String input = "image foo[[3,4]";
        thrown.expect(SyntaxException.class);
        try {
            Parser parser = makeParser(input);
            parser.parse();
        } catch (SyntaxException e) {
            show(e);
            assertEquals("Syntax Error: Wrong expression syntax/symbol for: image at position: 1 in line: 1", e.getMessage());                                                                                // expected position
            throw e;
        }
    }


}
