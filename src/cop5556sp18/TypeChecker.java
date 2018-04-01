package cop5556sp18;

import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Types.Type;

import java.util.List;

public class TypeChecker implements ASTVisitor {

    TypeChecker() {
    }

    @SuppressWarnings("serial")
    public static class SemanticException extends Exception {
        Token t;

        public SemanticException(Token t, String message) {
            super(message);
            this.t = t;
        }
    }

    SymbolTable symbolTable = new SymbolTable();

    // Name is only used for naming the output file.
    // Visit the child block to type check program.
    // no need of symbol table here as scope starts after declaration of name of
    // program
    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        program.block.visit(this, arg);
        return null;
    }

    /**
     * Increments the scope number on visit of each new block.
     */
    @Override
    public Object visitBlock(Block block, Object arg) throws Exception {
        symbolTable.enterScope();
        List<ASTNode> decsOrStatements = block.decsOrStatements;
        for (ASTNode node : decsOrStatements) {
            node.visit(this, arg);
        }
        symbolTable.closeScope();
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
        Token firstToken = declaration.firstToken;
        Boolean status = symbolTable.insert(declaration.name, declaration);
        Expression height = declaration.height;
        Expression width = declaration.width;

        // if height and width are present, the type should be image
        if (height != null || width != null) {
            if (Types.getType(declaration.type) != Type.IMAGE) {
                throw new SemanticException(firstToken,
                        String.format("Line: %s Pos: %s \t Error: Incompatible type. Should be image.",
                                firstToken.line(), firstToken.posInLine()));
            }
            if (height == null || width == null) {
                throw new SemanticException(firstToken,
                        String.format("Line: %s Pos: %s \t Error: Width and height both need to be declared",
                                firstToken.line(), firstToken.posInLine()));
            }

            height.visit(this, arg);
            width.visit(this, arg);
            if (height.type != Type.INTEGER || width.type != Type.INTEGER) {
                throw new SemanticException(firstToken,
                        String.format("Line: %s Pos: %s \t Error: Width and height both need integers",
                                firstToken.line(), firstToken.posInLine()));
            }
        }
        System.out.println("SYM: " + symbolTable.toString());
        if (!status) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: Duplicate declaration within same scope",
                            firstToken.line(), firstToken.posInLine()));
        }
        return null;
    }

    @Override
    public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
        Token firstToken = statementWrite.firstToken;
        Declaration sourceDec = symbolTable.lookup(statementWrite.sourceName);
        System.out.println("sourceDec: " + sourceDec);
        if (sourceDec != null) {
            Declaration destDec = symbolTable.lookup(statementWrite.destName);
            if (destDec != null) {
                if (sourceDec.type != Kind.KW_image) {
                    throw new SemanticException(firstToken,
                            String.format(
                                    "Line: %s Pos: %s \t Error: Imcompatible source type for write: should be image",
                                    firstToken.line(), firstToken.posInLine()));
                }
                if (destDec.type != Kind.KW_filename) {
                    throw new SemanticException(firstToken, String.format(
                            "Line: %s Pos: %s \t Error: Imcompatible destination type for write: should be filename",
                            firstToken.line(), firstToken.posInLine()));
                }
            } else {
                throw new SemanticException(firstToken,
                        String.format("Line: %s Pos: %s \t Error: No declaration found for destination type in write",
                                firstToken.line(), firstToken.posInLine()));
            }
        } else {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: No declaration found for source type in write",
                            firstToken.line(), firstToken.posInLine()));
        }
        System.out.println("SYM: " + symbolTable.toString());
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
        Token firstToken = statementInput.firstToken;
        Declaration dec = symbolTable.lookup(statementInput.destName);
        System.out.println("SYM: " + symbolTable.toString());
        if (dec != null) {
            statementInput.e.visit(this, arg);
            // have to check if visit method assigns type as input or not
            if (statementInput.e.type != Type.INTEGER) {
                throw new SemanticException(firstToken,
                        String.format("Line: %s Pos: %s \t Error: Input expression after @ needs to be an integer",
                                firstToken.line(), firstToken.posInLine()));
            }
        } else {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: No declaration found for destination in input",
                            firstToken.line(), firstToken.posInLine()));
        }
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        Token firstToken = pixelSelector.firstToken;
        pixelSelector.ex.visit(this, arg);
        pixelSelector.ey.visit(this, arg);
        if (pixelSelector.ex.type != pixelSelector.ey.type) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: Assigned expressions should be of same type",
                            firstToken.line(), firstToken.posInLine()));
        }
        if (pixelSelector.ex.type != Type.INTEGER && pixelSelector.ex.type != Type.FLOAT) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: Expression type should be integer or float",
                            firstToken.line(), firstToken.posInLine()));
        }
        return null;
    }

    @Override
    public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
        Token firstToken = expressionConditional.firstToken;
        expressionConditional.guard.visit(this, arg);
        expressionConditional.trueExpression.visit(this, arg);
        expressionConditional.falseExpression.visit(this, arg);
        if (expressionConditional.guard.type != Type.BOOLEAN) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: Incorrect value of condition. Should be boolean",
                            firstToken.line(), firstToken.posInLine()));
        }
        if (expressionConditional.trueExpression.type != expressionConditional.falseExpression.type) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: Incompatible return types for condition. Should be same",
                            firstToken.line(), firstToken.posInLine()));
        }
        expressionConditional.type = expressionConditional.trueExpression.type;
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
        Token firstToken = expressionBinary.firstToken;
        expressionBinary.leftExpression.visit(this, arg);
        expressionBinary.rightExpression.visit(this, arg);
        Type leftExprType = expressionBinary.leftExpression.type;
        Type rightExprType = expressionBinary.rightExpression.type;
        Type inferredType;
        switch (expressionBinary.op) {
            case OP_PLUS:
            case OP_MINUS:
            case OP_TIMES:
            case OP_DIV:
            case OP_POWER:
                if (leftExprType == Type.INTEGER && rightExprType == Type.INTEGER) {
                    inferredType = Type.INTEGER;
                } else if (leftExprType == Type.FLOAT && rightExprType == Type.FLOAT) {
                    inferredType = Type.FLOAT;
                } else if (leftExprType == Type.FLOAT && rightExprType == Type.INTEGER) {
                    inferredType = Type.FLOAT;
                } else if (leftExprType == Type.INTEGER && rightExprType == Type.FLOAT) {
                    inferredType = Type.FLOAT;
                } else {
                    throw new SemanticException(firstToken, String.format(
                            "Line: %s Pos: %s \t Error: Incompatible types around operator. Allowed only integer and float",
                            firstToken.line(), firstToken.posInLine()));
                }
                break;
            case OP_MOD:
            	if (leftExprType == Type.INTEGER && rightExprType == Type.INTEGER) {
                    inferredType = Type.INTEGER;
                } else {
                    throw new SemanticException(firstToken, String.format(
                            "Line: %s Pos: %s \t Error: Incompatible types around operator. Allowed only integer",
                            firstToken.line(), firstToken.posInLine()));
                }
            	break;
            case OP_AND:
            case OP_OR:
                if (leftExprType == Type.INTEGER && rightExprType == Type.INTEGER) {
                    inferredType = Type.INTEGER;
                } else if (leftExprType == Type.BOOLEAN && rightExprType == Type.BOOLEAN) {
                    inferredType = Type.BOOLEAN;
                } else {
                    throw new SemanticException(firstToken, String.format(
                            "Line: %s Pos: %s \t Error: Incompatible types around operator. Allowed only integer and boolean",
                            firstToken.line(), firstToken.posInLine()));
                }
                break;
            case OP_EQ:
            case OP_NEQ:
            case OP_GT:
            case OP_GE:
            case OP_LT:
            case OP_LE:
                if (leftExprType == Type.INTEGER && rightExprType == Type.INTEGER) {
                    inferredType = Type.BOOLEAN;
                } else if (leftExprType == Type.FLOAT && rightExprType == Type.FLOAT) {
                    inferredType = Type.BOOLEAN;
                } else if (leftExprType == Type.BOOLEAN && rightExprType == Type.BOOLEAN) {
                    inferredType = Type.BOOLEAN;
                } else {
                    throw new SemanticException(firstToken, String.format(
                            "Line: %s Pos: %s \t Error: Incompatible types around operator. Allowed only integer, float and boolean",
                            firstToken.line(), firstToken.posInLine()));
                }
                break;
            default:
                throw new SemanticException(firstToken, String.format(
                        "Line: %s Pos: %s \t Error BinaryExpression: Invalid or Incompatible types around operator.",
                        firstToken.line(), firstToken.posInLine()));
        }
        expressionBinary.type = inferredType;
        return null;
    }

    @Override
    public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
        expressionUnary.expression.visit(this, arg);
        // exception not thrown as expression can be anything
        expressionUnary.type = expressionUnary.expression.type;
        return null;
    }

    @Override
    public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
            throws Exception {
        expressionIntegerLiteral.type = Type.INTEGER;
        return null;
    }

    @Override
    public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
        expressionBooleanLiteral.type = Type.BOOLEAN;
        return null;
    }

    @Override
    public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
            throws Exception {
        expressionPredefinedName.type = Type.INTEGER;
        return null;
    }

    @Override
    public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
            throws Exception {
        expressionFloatLiteral.type = Type.FLOAT;
        return null;
    }

    @Override
    public Object visitExpressionFunctionAppWithExpressionArg(ExpressionFunctionAppWithExpressionArg expression,
                                                              Object arg) throws Exception {
        Token firstToken = expression.firstToken;
        Type inferredType;
        expression.e.visit(this, arg);
        switch (expression.e.type) {
            case INTEGER: {
                if (expression.function == Scanner.Kind.KW_abs || expression.function == Scanner.Kind.KW_red
                        || expression.function == Scanner.Kind.KW_green || expression.function == Scanner.Kind.KW_blue
                        || expression.function == Scanner.Kind.KW_alpha) {
                    inferredType = Type.INTEGER;
                } else if (expression.function == Scanner.Kind.KW_float) {
                    inferredType = Type.FLOAT;
                } else if (expression.function == Scanner.Kind.KW_int) {
                    inferredType = Type.INTEGER;
                } else {
                    throw new SemanticException(firstToken,
                            String.format(
                                    "Line: %s Pos: %s \t Error: Invalid or Incompatible types for function and expression.",
                                    firstToken.line(), firstToken.posInLine()));
                }
            }
            break;
            case FLOAT: {
                if (expression.function == Scanner.Kind.KW_abs || expression.function == Scanner.Kind.KW_sin
                        || expression.function == Scanner.Kind.KW_cos || expression.function == Scanner.Kind.KW_atan
                        || expression.function == Scanner.Kind.KW_log) {
                    inferredType = Type.FLOAT;
                } else if (expression.function == Scanner.Kind.KW_float) {
                    inferredType = Type.FLOAT;
                } else if (expression.function == Scanner.Kind.KW_int) {
                    inferredType = Type.INTEGER;
                } else {
                    throw new SemanticException(firstToken,
                            String.format(
                                    "Line: %s Pos: %s \t Error: Invalid or Incompatible types for function and expression.",
                                    firstToken.line(), firstToken.posInLine()));
                }
            }
            break;
            case IMAGE: {
                if (expression.function == Scanner.Kind.KW_width || expression.function == Scanner.Kind.KW_height) {
                    inferredType = Type.INTEGER;
                } else {
                    throw new SemanticException(firstToken,
                            String.format(
                                    "Line: %s Pos: %s \t Error: Invalid or Incompatible types for function and expression.",
                                    firstToken.line(), firstToken.posInLine()));
                }
            }
            break;
            default:
                throw new SemanticException(firstToken,
                        String.format(
                                "Line: %s Pos: %s \t Error: Invalid or Incompatible types for function and expression.",
                                firstToken.line(), firstToken.posInLine()));
        }
        expression.type = inferredType;
        return null;
    }

    @Override
    public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel exprPixel, Object arg)
            throws Exception {
        Token firstToken = exprPixel.firstToken;
        exprPixel.e0.visit(this, arg);
        exprPixel.e1.visit(this, arg);
        if (exprPixel.name == Scanner.Kind.KW_cart_x || exprPixel.name == Scanner.Kind.KW_cart_y) {
            if (exprPixel.e0.type != Type.FLOAT || exprPixel.e1.type != Type.FLOAT) {
                throw new SemanticException(firstToken, String.format(
                        "Line: %s Pos: %s \t Error in function cart_x or cart_y: Both function expressions should be of type float",
                        firstToken.line(), firstToken.posInLine()));
            }
            exprPixel.type = Type.INTEGER;
        } else if (exprPixel.name == Scanner.Kind.KW_polar_a || exprPixel.name == Scanner.Kind.KW_polar_r) {
            if (exprPixel.e0.type != Type.INTEGER || exprPixel.e1.type != Type.INTEGER) {
                throw new SemanticException(firstToken, String.format(
                        "Line: %s Pos: %s \t Error in function polar_a or polar_r: Both function expressions should be of type integer",
                        firstToken.line(), firstToken.posInLine()));
            }
            exprPixel.type = Type.FLOAT;
        } else {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: Invalid function name or no such function defined.",
                            firstToken.line(), firstToken.posInLine()));
        }
        return null;
    }

    @Override
    public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
            throws Exception {
        Token firstToken = expressionPixelConstructor.firstToken;
        expressionPixelConstructor.alpha.visit(this, arg);
        expressionPixelConstructor.red.visit(this, arg);
        expressionPixelConstructor.green.visit(this, arg);
        expressionPixelConstructor.blue.visit(this, arg);
        if (expressionPixelConstructor.alpha.type != Type.INTEGER || expressionPixelConstructor.red.type != Type.INTEGER
                || expressionPixelConstructor.green.type != Type.INTEGER
                || expressionPixelConstructor.blue.type != Type.INTEGER) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: Image pixel values should be integer", firstToken.line(),
                            firstToken.posInLine()));
        }
        expressionPixelConstructor.type = Type.INTEGER;
        return null;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
        Token firstToken = statementAssign.firstToken;
        LHS lhs = statementAssign.lhs;
        Expression expr = statementAssign.e;
        lhs.visit(this, arg);
        expr.visit(this, arg);
        if (lhs.type != expr.type) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: Incompatible LHS and RHS types", firstToken.line(),
                            firstToken.posInLine()));
        }
        return null;
    }

    @Override
    public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
        Token firstToken = statementShow.firstToken;
        statementShow.e.visit(this, arg);
        if (statementShow.e.type != Type.INTEGER && statementShow.e.type != Type.BOOLEAN
                && statementShow.e.type != Type.FLOAT && statementShow.e.type != Type.IMAGE) {
            throw new SemanticException(firstToken,
                    String.format(
                            "Line: %s Pos: %s \t Error: Wrong expression type. Allowed: int, boolean, float, and image",
                            firstToken.line(), firstToken.posInLine()));
        }
        return null;
    }

    @Override
    public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
        Token firstToken = expressionPixel.firstToken;
        String identifier = expressionPixel.name;
        expressionPixel.declaration = symbolTable.lookup(identifier);
        if (expressionPixel.declaration == null) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: No declaration found for identifier", firstToken.line(),
                            firstToken.posInLine()));
        }
        if (Types.getType(expressionPixel.declaration.type) != Type.IMAGE) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error ExpressionPixel: Declaration type should be image.",
                            firstToken.line(), firstToken.posInLine()));
        }
        expressionPixel.type = Type.INTEGER;
        expressionPixel.pixelSelector.visit(this, arg);
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
        Token firstToken = expressionIdent.firstToken;
        expressionIdent.declaration = symbolTable.lookup(expressionIdent.name);
        if (expressionIdent.declaration == null) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: No declaration found for identifier", firstToken.line(),
                            firstToken.posInLine()));
        }
        expressionIdent.type = Types.getType(expressionIdent.declaration.type);
        return null;
    }

    @Override
    public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
        Token firstToken = lhsSample.firstToken;
        String lhsSampleName = lhsSample.name;
        lhsSample.declaration = symbolTable.lookup(lhsSampleName);
        if (lhsSample.declaration == null) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error LHSSample: No declaration found for identifier",
                            firstToken.line(), firstToken.posInLine()));
        }
        if (Types.getType(lhsSample.declaration.type) != Type.IMAGE) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error LHSSample: Declaration type should be image.",
                            firstToken.line(), firstToken.posInLine()));
        }
        lhsSample.type = Type.INTEGER;
        lhsSample.pixelSelector.visit(this, arg);
        return null;
    }

    @Override
    public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
        Token firstToken = lhsPixel.firstToken;
        String lhsPixelName = lhsPixel.name;
        lhsPixel.declaration = symbolTable.lookup(lhsPixelName);
        if (lhsPixel.declaration == null) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error LHSPixel: No declaration found for identifier",
                            firstToken.line(), firstToken.posInLine()));
        }
        if (Types.getType(lhsPixel.declaration.type) != Type.IMAGE) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error LHSPixel: Declaration type should be image.",
                            firstToken.line(), firstToken.posInLine()));
        }

        lhsPixel.type = Type.INTEGER;
        lhsPixel.pixelSelector.visit(this, arg);
        return null;
    }

    @Override
    public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
        Token firstToken = lhsIdent.firstToken;
        String lhsIdentName = lhsIdent.name;
        lhsIdent.declaration = symbolTable.lookup(lhsIdentName);
        if (lhsIdent.declaration == null) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: No declaration found for identifier in LHS",
                            firstToken.line(), firstToken.posInLine()));
        }
        lhsIdent.type = Types.getType(lhsIdent.declaration.type);
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
        Token firstToken = statementIf.firstToken;
        Expression expr = statementIf.guard;
        expr.visit(this, arg);
        if (expr.type != Type.BOOLEAN) {
            throw new SemanticException(firstToken,
                    String.format(
                            "Line: %s Pos: %s \t Error If: Incorrect return value for expression. Should be boolean",
                            firstToken.line(), firstToken.posInLine()));
        }
        statementIf.b.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
        Token firstToken = statementWhile.firstToken;
        Expression expr = statementWhile.guard;
        expr.visit(this, arg);
        if (expr.type != Type.BOOLEAN) {
            throw new SemanticException(firstToken,
                    String.format(
                            "Line: %s Pos: %s \t Error While: Incorrect return value for expression. Should be boolean",
                            firstToken.line(), firstToken.posInLine()));
        }
        statementWhile.b.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
        Token firstToken = statementSleep.firstToken;
        statementSleep.duration.visit(this, arg);
        if (statementSleep.duration.type != Type.INTEGER) {
            throw new SemanticException(firstToken,
                    String.format("Line: %s Pos: %s \t Error: Wrong sleep argument type. Allowed: integer",
                            firstToken.line(), firstToken.posInLine()));
        }
        return null;
    }

}
