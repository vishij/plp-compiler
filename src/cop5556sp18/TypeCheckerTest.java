package cop5556sp18;

import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.TypeChecker.SemanticException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TypeCheckerTest {

    /*
     * set Junit to be able to catch exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Prints objects in a way that is easy to turn on and off
     */
    static final boolean doPrint = true;

    private void show(Object input) {
        if (doPrint) {
            System.out.println(input.toString());
        }
    }

    /**
     * Scans, parses, and type checks the input string
     *
     * @param input
     * @throws Exception
     */
    void typeCheck(String input) throws Exception {
        show(input);
        // instantiate a Scanner and scan input
        Scanner scanner = new Scanner(input).scan();
        show(scanner);
        // instantiate a Parser and parse input to obtain and AST
        Program ast = new Parser(scanner).parse();
        show(ast);
        // instantiate a TypeChecker and visit the ast to perform type checking and
        // decorate the AST.
        ASTVisitor v = new TypeChecker();
        ast.visit(v, null);
    }

    /**
     * Simple test case with an almost empty program.
     *
     * @throws Exception
     */
    @Test
    public void emptyProg() throws Exception {
        String input = "emptyProg{}";
        typeCheck(input);
    }

    @Test
    public void expression1() throws Exception {
        String input = "prog {show 3+4;}";
        typeCheck(input);
    }

    @Test
    public void expression2_fail() throws Exception {
        String input = "prog { show true+4; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void declaration() throws Exception {
        String input = "b{int c; image j;}";
        typeCheck(input);
    }

    @Test
    public void declFail() throws Exception {
        String input = "b{int c; int c;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void declarationHeightWidth() throws Exception {
        String input = "prog {image a[11, 12];}";
        typeCheck(input);
    }

    @Test
    public void declarationHeightWidthVar() throws Exception {
        String input = "prog {int x; image a[x, 12];}";
        typeCheck(input);
    }

    @Test
    public void declarationHeightWidthFail() throws Exception {
        String input = "prog {image a[x, 12];}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void declarationTypeFail() throws Exception {
        String input = "prog {image a[10.00, 12];}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementWrite() throws Exception {
        String input = "prog { image x; filename y; write x to y; }";
        typeCheck(input);
    }

    @Test
    public void stmtWriteSourceFail() throws Exception {
        String input = "prog { int x; filename y; write x to y; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void stmtWriteDestFail() throws Exception {
        String input = "prog { image x; image y; write x to y; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void stmtWriteSourceDeclFail() throws Exception {
        String input = "prog {filename y; write x to y; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void stmtWriteDestDeclFail() throws Exception {
        String input = "prog { image x; write x to y; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementInput() throws Exception {
        String input = "prog { image h; input h from @0; }";
        typeCheck(input);
    }

    @Test
    public void statementInputIndexVar() throws Exception {
        String input = "prog { image h; int x; input h from @x; }";
        typeCheck(input);
    }

    @Test
    public void statementInputDeclFail() throws Exception {
        String input = "prog {input h from @0; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementInputTypeFail() throws Exception {
        String input = "prog {image h; input h from @x; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementInputIndexTypeFail() throws Exception {
        String input = "prog {image h; float x; input h from @x; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementAssign() throws Exception {
        String input = "prog { int x; float y; x := 0; y := 1.; }";
        typeCheck(input);
    }

    @Test
    public void statementAssign1() throws Exception {
        String input = "prog { int x; int y; y := x; }";
        typeCheck(input);
    }

    @Test
    public void statementAssignFail() throws Exception {
        String input = "prog { int x; float y; y := x; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementWhile() throws Exception {
        String input = "prog { int x; while(x < 100){};}";
        typeCheck(input);
    }

    @Test
    public void statementWhile1() throws Exception {
        String input = "b { while (true) {} ;}";
        typeCheck(input);
    }

    @Test
    public void statementWhileFail() throws Exception {
        String input = "prog { while(x < 100){};}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementWhileFail1() throws Exception {
        String input = "b { while (2) {} ;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementIf() throws Exception {
        String input = "prog { int x; if(x < 100){};}";
        typeCheck(input);
    }

    @Test
    public void statementIfFail() throws Exception {
        String input = "prog {if(x < 100){};}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementShow() throws Exception {
        String input = "prog { int x; show 1; show x; }";
        typeCheck(input);
    }

    @Test
    public void statementShowDeclFail() throws Exception {
        String input = "prog {show x;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementShowTypeFail() throws Exception {
        String input = "prog { filename x; show x; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void statementSleep() throws Exception {
        String input = "prog {sleep(4000); }";
        typeCheck(input);
    }

    @Test
    public void statementSleepFail() throws Exception {
        String input = "prog { sleep(40.00); }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void LHSPixelSelector() throws Exception {
        String input = "prog { image a; a[1,2] := 3; }";
        typeCheck(input);
    }

    @Test
    public void testDemo1() throws Exception {
        String input = "demo1 { \n" + "image h; \n" + "input h from @0; \n" + "show h; \n" + "sleep(4000); \n"
                + "image g[width(h),height(h)]; \n" + "int x; \n" + "x:=0; \n" + "while(x<width(g)){int y; \n"
                + "y:=0; \n" + "while(y<height(g)){g[x,y]:=h[y,x]; \n" + "y:=y+1; \n" + "}; \n" + "x:=x+1; \n" + "}; \n"
                + "show g; \n" + "sleep(4000); \n" + "}";
        typeCheck(input);
    }

    @Test
    public void makeRedImage() throws Exception {
        String input = "makeRedImage { \n" + "image im[256,256]; \n" + "int x; \n" + "int y; \n" + "x := 0; \n"
                + "y := 0; \n" + "while (x < width(im)) { \n" + "y := 0; \n" + "while (y < height(im)) { \n"
                + "im[x,y] := <<255,255,0,0>>; \n" + "y := y+1; \n" + "}; \n" + "x := x+1; \n" + "}; \n" + "show im; \n"
                + "}";
        typeCheck(input);
    }

    @Test
    public void testPolarR2() throws Exception {
        String input = "PolarR2 { \n" + "image im[1024,1024]; \n" + "int x; \n" + "x := 0; \n"
                + "while (x < width(im)) { \n" + "int y; \n" + "y := 0; \n" + "while (y < height(im)) { \n"
                + "float p; \n" + "p := polar_r[x,y]; \n" + "int r; \n" + "r := int(p) % Z; \n"
                + "im[x,y] := <<Z, 0, 0, r>>; \n" + "y := y+1; \n" + "}; \n" + "x := x + 1; \n" + "}; \n"
                + "show im; \n" + "}";
        typeCheck(input);
    }

    @Test
    public void testSamples() throws Exception {
        String input = "samples { \n" + "image bird; \n" + "input bird from @0; \n" + "show bird; \n"
                + "sleep(4000); \n" + "image bird2[width(bird),height(bird)]; \n" + "int x; \n" + "x:=0; \n"
                + "while(x<width(bird2)) {int y; \n" + "y:=0; \n"
                + "while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]); \n"
                + "green(bird2[x,y]):=blue(bird[x,y]); \n" + "red(bird2[x,y]):=green(bird[x,y]); \n"
                + "alpha(bird2[x,y]):=Z; \n" + "y:=y+1; \n" + "}; \n" + "x:=x+       1; \n" + "}; \n" + "show bird2; \n"
                + "sleep(4000); \n" + "}";
        typeCheck(input);
    }

    @Test
    public void testPolar() throws Exception {
        String input = "Polar {int x; int y; float p; p := polar_r[x,y];}";
        typeCheck(input);
    }

    @Test
    public void testPolarDeclFail() throws Exception {
        String input = "Polar {float p; p := polar_r[x,y];}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void testPolarTypeFail() throws Exception {
        String input = "Polar {int x; int y; int p; p := polar_r[x,y];}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void testCart() throws Exception {
        String input = "Polar {float x; float y; int p; p := cart_x[x,y];}";
        typeCheck(input);
    }

    @Test
    public void testCartDeclFail() throws Exception {
        String input = "Polar {int p; p := cart_x[x,y];}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void testCartTypeFail() throws Exception {
        String input = "Polar {int x; float y; int p; p := cart_x[x,y];}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void nestedDec() throws Exception {
        String input = "X{ int x; int y; while (x == y) {int x;}; }";
        typeCheck(input);
    }

    @Test
    public void nestedDec1() throws Exception {
        String input = "X{ int x; int y; while (x == y) { x := 2;}; }";
        typeCheck(input);
    }

    @Test
    public void nestedDec2() throws Exception {
        String input = "X{ int x; int z; while (x == y) {int x;}; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void nestedDec3() throws Exception {
        String input = "X{ int x; int y; while (x == y) { show x;}; }";
        typeCheck(input);
    }

    @Test
    public void nestedDec4() throws Exception {
        String input = "X{ int x; int y; while (x == y) { int z;}; show z;}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void testinvalidExpressionBinary1() throws Exception {
        String input = "prog{ show (1.0 % 2.0);}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }

    @Test
    public void testinvalidExpressionBinary2() throws Exception {
        String input = "prog{ show (1 % 2.0);}";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }
}