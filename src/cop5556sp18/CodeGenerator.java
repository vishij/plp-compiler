/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles
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
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Types.Type;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CodeGenerator implements ASTVisitor, Opcodes {

    /**
     * All methods and variable static.
     */

    static final int Z = 255;

    ClassWriter cw;
    String className;
    String classDesc;
    String sourceFileName;

    MethodVisitor mv; // visitor of method currently under construction

    /**
     * Indicates whether genPrint and genPrintTOS should generate code.
     */
    final boolean DEVEL;
    final boolean GRADE;

    final int defaultWidth;
    final int defaultHeight;
    final boolean itf = false;

    private Integer slotNumCounter;

    /**
     * @param DEVEL          used as parameter to genPrint and genPrintTOS
     * @param GRADE          used as parameter to genPrint and genPrintTOS
     * @param sourceFileName name of source file, may be null.
     * @param defaultWidth   default width of images
     * @param defaultHeight  default height of images
     */
    public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName, int defaultWidth, int defaultHeight) {
        super();
        this.DEVEL = DEVEL;
        this.GRADE = GRADE;
        this.sourceFileName = sourceFileName;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.slotNumCounter = 1;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws Exception {
        for (ASTNode node : block.decsOrStatements) {
            Label startBlockLabel = new Label();
            mv.visitLabel(startBlockLabel);
            node.visit(this, arg);
        }
        return null;
    }

    @Override
    public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
        mv.visitLdcInsn(expressionBooleanLiteral.value);
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
        declaration.slotNumber = slotNumCounter;
        slotNumCounter++;
        Type type = Types.getType(declaration.type);

        if (type.equals(Type.IMAGE)) {
            //TODO: label to be visited for all cases??
            if (declaration.width == null && declaration.height == null) {
                mv.visitLdcInsn(defaultWidth);
                mv.visitLdcInsn(defaultHeight);
            } else {
                declaration.width.visit(this, arg);
                declaration.height.visit(this, arg);
            }
            mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage",
                    RuntimeImageSupport.makeImageSig, itf);
            mv.visitVarInsn(ASTORE, declaration.slotNumber);
        } else if (type.equals(Type.INTEGER) || type.equals(Type.FLOAT) || type.equals(Type.BOOLEAN) || type.equals(Type.FILE)) {
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE, declaration.slotNumber);
        }
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
        expressionBinary.leftExpression.visit(this, arg);
        expressionBinary.rightExpression.visit(this, arg);
        Type leftExprType = expressionBinary.leftExpression.type;
        Type rightExprType = expressionBinary.rightExpression.type;
        switch (expressionBinary.op) {
            case OP_PLUS: {
                if (leftExprType.equals(Type.INTEGER) && rightExprType.equals(Type.INTEGER)) {
                    mv.visitInsn(IADD);
                } else if (leftExprType.equals(Type.FLOAT) && rightExprType.equals(Type.FLOAT)) {
                    mv.visitInsn(FADD);
                } else if (leftExprType.equals(Type.FLOAT) && rightExprType.equals(Type.INTEGER)) {
                    mv.visitInsn(I2F);
                    mv.visitInsn(FADD);
                } else if (leftExprType.equals(Type.INTEGER) && rightExprType.equals(Type.FLOAT)) {
                    // swap the top float with integer element below in order to convert the integer
                    // as float
                    mv.visitInsn(SWAP);
                    mv.visitInsn(I2F);
                    mv.visitInsn(FADD);
                }
            }
            break;
            case OP_MINUS: {
                if (leftExprType.equals(Type.INTEGER) && rightExprType.equals(Type.INTEGER)) {
                    mv.visitInsn(ISUB);
                } else if (leftExprType.equals(Type.FLOAT) && rightExprType.equals(Type.FLOAT)) {
                    mv.visitInsn(FSUB);
                } else if (leftExprType.equals(Type.FLOAT) && rightExprType.equals(Type.INTEGER)) {
                    mv.visitInsn(I2F);
                    mv.visitInsn(FSUB);
                } else if (leftExprType.equals(Type.INTEGER) && rightExprType.equals(Type.FLOAT)) {
                    // swap the top float with integer element below in order to convert the integer
                    // as float and swap again to get the correct value of expression
                    mv.visitInsn(SWAP);
                    mv.visitInsn(I2F);
                    mv.visitInsn(SWAP);
                    mv.visitInsn(FSUB);
                }
            }
            break;
            case OP_TIMES: {
                if (leftExprType.equals(Type.INTEGER) && rightExprType.equals(Type.INTEGER)) {
                    mv.visitInsn(IMUL);
                } else if (leftExprType.equals(Type.FLOAT) && rightExprType.equals(Type.FLOAT)) {
                    mv.visitInsn(FMUL);
                } else if (leftExprType.equals(Type.FLOAT) && rightExprType.equals(Type.INTEGER)) {
                    mv.visitInsn(I2F);
                    mv.visitInsn(FMUL);
                } else if (leftExprType.equals(Type.INTEGER) && rightExprType.equals(Type.FLOAT)) {
                    // swap the top float with integer element below in order to convert the integer
                    // as float; no need to swap again after conversion due to commutative property
                    // of multiplication
                    mv.visitInsn(SWAP);
                    mv.visitInsn(I2F);
                    mv.visitInsn(FMUL);
                }
            }
            break;
            case OP_DIV: {
                if (leftExprType.equals(Type.INTEGER) && rightExprType.equals(Type.INTEGER)) {
                    mv.visitInsn(IDIV);
                } else if (leftExprType.equals(Type.FLOAT) && rightExprType.equals(Type.FLOAT)) {
                    mv.visitInsn(FDIV);
                } else if (leftExprType.equals(Type.FLOAT) && rightExprType.equals(Type.INTEGER)) {
                    mv.visitInsn(I2F);
                    mv.visitInsn(FDIV);
                } else if (leftExprType.equals(Type.INTEGER) && rightExprType.equals(Type.FLOAT)) {
                    // swap the top float with integer element below in order to convert the integer
                    // as float; swap back so that numerator and denominator are in correct place -
                    // left and right expr
                    mv.visitInsn(SWAP);
                    mv.visitInsn(I2F);
                    mv.visitInsn(SWAP);
                    mv.visitInsn(FDIV);
                }
            }
            break;
            case OP_POWER: {
                if (leftExprType.equals(Type.INTEGER) && rightExprType.equals(Type.INTEGER)) {
                    mv.visitInsn(POP);
                    mv.visitInsn(POP);
                    expressionBinary.leftExpression.visit(this, arg);
                    mv.visitInsn(I2D);
                    expressionBinary.rightExpression.visit(this, arg);
                    mv.visitInsn(I2D);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", itf);
                    mv.visitInsn(D2I);
                } else if (leftExprType.equals(Type.FLOAT) && rightExprType.equals(Type.FLOAT)) {
                    mv.visitInsn(POP);
                    mv.visitInsn(POP);
                    expressionBinary.leftExpression.visit(this, arg);
                    mv.visitInsn(F2D);
                    expressionBinary.rightExpression.visit(this, arg);
                    mv.visitInsn(F2D);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", itf);
                    mv.visitInsn(D2F);
                } else if (leftExprType.equals(Type.FLOAT) && rightExprType.equals(Type.INTEGER)) {
                    mv.visitInsn(POP);
                    mv.visitInsn(POP);
                    expressionBinary.leftExpression.visit(this, arg);
                    mv.visitInsn(F2D);
                    expressionBinary.rightExpression.visit(this, arg);
                    mv.visitInsn(I2D);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", itf);
                    mv.visitInsn(D2F);
                } else if (leftExprType.equals(Type.INTEGER) && rightExprType.equals(Type.FLOAT)) {
                    mv.visitInsn(POP);
                    mv.visitInsn(POP);
                    expressionBinary.leftExpression.visit(this, arg);
                    mv.visitInsn(I2D);
                    expressionBinary.rightExpression.visit(this, arg);
                    mv.visitInsn(F2D);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", itf);
                    mv.visitInsn(D2F);
                }
            }
            break;
            case OP_MOD: {
                // if conditions not needed to be checked as typechecker already does this -
                // only one condition here
                mv.visitInsn(IREM);
            }
            break;
            case OP_AND: {
                mv.visitInsn(IAND);
            }
            break;
            case OP_OR: {
                mv.visitInsn(IOR);
            }
            break;
            case OP_EQ: {
                if (leftExprType.equals(Type.INTEGER) || leftExprType.equals(Type.BOOLEAN)) {
                    getConditionalByteCode(IF_ICMPEQ);
                } else if (leftExprType.equals(Type.FLOAT)) {
                    getFloatConditonalByteCode(IFEQ);
                }
            }
            break;
            case OP_NEQ: {
                if (leftExprType.equals(Type.INTEGER) || leftExprType.equals(Type.BOOLEAN)) {
                    getConditionalByteCode(IF_ICMPNE);
                } else if (leftExprType.equals(Type.FLOAT)) {
                    getFloatConditonalByteCode(IFNE);
                }
            }
            break;
            case OP_GT: {
                if (leftExprType.equals(Type.INTEGER) || leftExprType.equals(Type.BOOLEAN)) {
                    getConditionalByteCode(IF_ICMPGT);
                } else if (leftExprType.equals(Type.FLOAT)) {
                    getFloatConditonalByteCode(IFGT);
                }
            }
            break;
            case OP_GE: {
                if (leftExprType.equals(Type.INTEGER) || leftExprType.equals(Type.BOOLEAN)) {
                    getConditionalByteCode(IF_ICMPGE);
                } else if (leftExprType.equals(Type.FLOAT)) {
                    getFloatConditonalByteCode(IFGE);
                }
            }
            break;
            case OP_LT: {
                if (leftExprType.equals(Type.INTEGER) || leftExprType.equals(Type.BOOLEAN)) {
                    getConditionalByteCode(IF_ICMPLT);
                } else if (leftExprType.equals(Type.FLOAT)) {
                    getFloatConditonalByteCode(IFLT);
                }
            }
            break;
            case OP_LE: {
                if (leftExprType.equals(Type.INTEGER) || leftExprType.equals(Type.BOOLEAN)) {
                    getConditionalByteCode(IF_ICMPLE);
                } else if (leftExprType.equals(Type.FLOAT)) {
                    getFloatConditonalByteCode(IFLE);
                }
            }
            break;
            default:
        }
        return null;
    }

    private void getFloatConditonalByteCode(Integer jumpOpCode) {
        Label trueLabel = new Label();
        mv.visitInsn(FCMPL);
        mv.visitJumpInsn(jumpOpCode, trueLabel);
        mv.visitInsn(ICONST_0);
        Label falseLabel = new Label();
        mv.visitJumpInsn(GOTO, falseLabel);

        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(falseLabel);
    }

    private void getConditionalByteCode(Integer opcode) {
        Label trueLabel = new Label();
        mv.visitJumpInsn(opcode, trueLabel);
        mv.visitInsn(ICONST_0);
        Label falseLabel = new Label();
        mv.visitJumpInsn(GOTO, falseLabel);

        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(falseLabel);
    }

    @Override
    public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
        Label condLabel1 = new Label();
        Label condLabel2 = new Label();
        expressionConditional.guard.visit(this, arg);
        mv.visitJumpInsn(IFEQ, condLabel1);
        expressionConditional.trueExpression.visit(this, arg);
        mv.visitJumpInsn(GOTO, condLabel2);

        mv.visitLabel(condLabel1);
        expressionConditional.falseExpression.visit(this, arg);
        mv.visitLabel(condLabel2);
        return null;
    }

    @Override
    public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
            throws Exception {
        mv.visitLdcInsn(expressionFloatLiteral.value);
        return null;
    }

    @Override
    public Object visitExpressionFunctionAppWithExpressionArg(ExpressionFunctionAppWithExpressionArg expression,
                                                              Object arg) throws Exception {
        expression.e.visit(this, arg);
        Kind fxn = expression.function;
        Type type = expression.e.type;
        if (fxn.equals(Scanner.Kind.KW_abs)) {
            if (type.equals(Type.FLOAT)) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", itf);
            } else if (type.equals(Type.INTEGER)) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", itf);
            }
        } else if (fxn.equals(Scanner.Kind.KW_red)) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getRed", RuntimePixelOps.getRedSig,
                    itf);
        } else if (fxn.equals(Scanner.Kind.KW_green)) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getGreen", RuntimePixelOps.getGreenSig,
                    itf);
        } else if (fxn.equals(Scanner.Kind.KW_blue)) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getBlue", RuntimePixelOps.getBlueSig,
                    itf);
        } else if (fxn.equals(Scanner.Kind.KW_alpha)) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getAlpha", RuntimePixelOps.getAlphaSig,
                    itf);
        } else if (fxn.equals(Scanner.Kind.KW_sin)) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", itf);
            mv.visitInsn(D2F);
        } else if (fxn.equals(Scanner.Kind.KW_cos)) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", itf);
            mv.visitInsn(D2F);
        } else if (fxn.equals(Scanner.Kind.KW_atan)) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", itf);
            mv.visitInsn(D2F);
        } else if (fxn.equals(Scanner.Kind.KW_log)) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", itf);
            mv.visitInsn(D2F);
        } else if (fxn.equals(Scanner.Kind.KW_float)) {
            if (type.equals(Type.INTEGER)) {
                mv.visitInsn(I2F);
            }

        } else if (fxn.equals(Scanner.Kind.KW_int)) {
            if (type.equals(Type.FLOAT)) {
                mv.visitInsn(F2I);
            }

        } else if (fxn.equals(Scanner.Kind.KW_width)) {
            if (type.equals(Type.IMAGE)) {
                mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getWidth", RuntimeImageSupport.getWidthSig,
                        itf);
            }

        } else if (fxn.equals(Scanner.Kind.KW_height)) {
            if (type.equals(Type.IMAGE)) {
                mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getHeight", RuntimeImageSupport.getHeightSig,
                        itf);
            }
        }
        return null;
    }

    @Override
    public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel exprPixel,
                                                      Object arg) throws Exception {
        exprPixel.e0.visit(this, arg);
        exprPixel.e1.visit(this, arg);
        Kind fxn = exprPixel.name;
        Type type = exprPixel.type;
        if (fxn.equals(Scanner.Kind.KW_cart_x)) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", itf);
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
            mv.visitInsn(F2I);
        } else if (fxn.equals(Scanner.Kind.KW_cart_y)) {
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", itf);
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
            mv.visitInsn(F2I);
        } else if (fxn.equals(Scanner.Kind.KW_polar_a)) {
            mv.visitInsn(POP);
            mv.visitInsn(POP);
            exprPixel.e1.visit(this, arg);
            mv.visitInsn(I2D);
            exprPixel.e0.visit(this, arg);
            mv.visitInsn(I2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", itf);
            mv.visitInsn(D2F);
        } else if (fxn.equals(Scanner.Kind.KW_polar_r)) {
            mv.visitInsn(POP);
            mv.visitInsn(POP);
            exprPixel.e0.visit(this, arg);
            mv.visitInsn(I2D);
            exprPixel.e1.visit(this, arg);
            mv.visitInsn(I2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", itf);
            mv.visitInsn(D2F);
        }
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
        if (expressionIdent.type.equals(Type.INTEGER) || expressionIdent.type.equals(Type.BOOLEAN)) {
            mv.visitVarInsn(ILOAD, expressionIdent.declaration.slotNumber);
        } else if (expressionIdent.type.equals(Type.FLOAT)) {
            mv.visitVarInsn(FLOAD, expressionIdent.declaration.slotNumber);
        } else if (expressionIdent.type.equals(Type.IMAGE) || expressionIdent.type.equals(Type.FILE)) {
            mv.visitVarInsn(ALOAD, expressionIdent.declaration.slotNumber);
        }
        return null;
    }

    @Override
    public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
            throws Exception {
        // This one is all done!
        mv.visitLdcInsn(expressionIntegerLiteral.value);
        return null;
    }

    @Override
    public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
        mv.visitVarInsn(ALOAD, expressionPixel.declaration.slotNumber);
        if (expressionPixel.pixelSelector.ex.type.equals(Type.INTEGER)) {
            expressionPixel.pixelSelector.visit(this, arg);
        }
        if (expressionPixel.pixelSelector.ex.type.equals(Type.FLOAT)) {
            expressionPixel.pixelSelector.visit(this, arg);
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", itf);
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
            mv.visitInsn(F2I);
        }
        if (expressionPixel.pixelSelector.ey.type.equals(Type.FLOAT)) {
            expressionPixel.pixelSelector.visit(this, arg);
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", itf);
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
            mv.visitInsn(F2I);
        }
        mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getPixel", RuntimeImageSupport.getPixelSig,
                itf);
        return null;
    }

    @Override
    public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
            throws Exception {
        expressionPixelConstructor.alpha.visit(this, arg);
        expressionPixelConstructor.red.visit(this, arg);
        expressionPixelConstructor.green.visit(this, arg);
        expressionPixelConstructor.blue.visit(this, arg);
        mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "makePixel", RuntimePixelOps.makePixelSig,
                itf);
        return null;
    }

    @Override
    public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
            throws Exception {
        if (expressionPredefinedName.name.equals(Kind.KW_Z)) {
            mv.visitLdcInsn(Z);
        } else if (expressionPredefinedName.name.equals(Kind.KW_default_height)) {
            mv.visitLdcInsn(defaultHeight);
        } else if (expressionPredefinedName.name.equals(Kind.KW_default_width)) {
            mv.visitLdcInsn(defaultWidth);
        }
        return null;
    }

    @Override
    public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
        expressionUnary.expression.visit(this, arg);
        Type type = expressionUnary.expression.type;
        if (expressionUnary.op.equals(Kind.OP_PLUS)) {
            // do nothing
        } else if (expressionUnary.op.equals(Kind.OP_MINUS)) {
            if (type.equals(Type.INTEGER)) {
                mv.visitInsn(INEG);
            } else if (type.equals(Type.FLOAT)) {
                mv.visitInsn(FNEG);
            }
        } else if (expressionUnary.op.equals(Kind.OP_EXCLAMATION)) {
            // uses -1 for flipping all bits in integer
            if (type.equals(Type.INTEGER)) {
                mv.visitInsn(ICONST_M1);
                mv.visitInsn(IXOR);
            } else if (type.equals(Type.BOOLEAN)) {
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IXOR);
            }
        }
        return null;
    }

    @Override
    public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
        Type type = lhsIdent.type;
        if (type.equals(Type.IMAGE)) {
            mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "deepCopy", RuntimeImageSupport.deepCopySig,
                    itf);
            mv.visitVarInsn(ASTORE, lhsIdent.declaration.slotNumber);
        } else if (type.equals(Type.INTEGER)) {
            mv.visitVarInsn(ISTORE, lhsIdent.declaration.slotNumber);
        } else if (type.equals(Type.FLOAT)) {
            mv.visitVarInsn(FSTORE, lhsIdent.declaration.slotNumber);
        } else if (type.equals(Type.BOOLEAN)) {
            mv.visitVarInsn(ISTORE, lhsIdent.declaration.slotNumber);
        } else if (type.equals(Type.FILE)) {
            mv.visitVarInsn(ASTORE, lhsIdent.declaration.slotNumber);
        }
        return null;
    }

    @Override
    public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
        mv.visitVarInsn(ALOAD, lhsPixel.declaration.slotNumber);
        if (lhsPixel.pixelSelector.ex.type.equals(Type.INTEGER)) {
            lhsPixel.pixelSelector.visit(this, arg);
        }
        if (lhsPixel.pixelSelector.ex.type.equals(Type.FLOAT)) {
            lhsPixel.pixelSelector.visit(this, arg);
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", itf);
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
            mv.visitInsn(F2I);
        }
        if (lhsPixel.pixelSelector.ey.type.equals(Type.FLOAT)) {
            lhsPixel.pixelSelector.visit(this, arg);
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", itf);
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
            mv.visitInsn(F2I);
        }
        mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "setPixel", RuntimeImageSupport.setPixelSig,
                itf);
        return null;
    }

    @Override
    public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
        mv.visitVarInsn(ALOAD, lhsSample.declaration.slotNumber);
        if (lhsSample.pixelSelector.ex.type.equals(Type.INTEGER)) {
            lhsSample.pixelSelector.visit(this, arg);
        }
        if (lhsSample.pixelSelector.ex.type.equals(Type.FLOAT)) {
            lhsSample.pixelSelector.visit(this, arg);
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", itf);
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
            mv.visitInsn(F2I);
        }
        if (lhsSample.pixelSelector.ey.type.equals(Type.FLOAT)) {
            lhsSample.pixelSelector.visit(this, arg);
            mv.visitInsn(F2D);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", itf);
            mv.visitInsn(D2F);
            mv.visitInsn(FMUL);
            mv.visitInsn(F2I);
        }
        if (lhsSample.color.equals(Kind.KW_alpha)) {
            mv.visitInsn(ICONST_0);
        } else if (lhsSample.color.equals(Kind.KW_red)) {
            mv.visitInsn(ICONST_1);
        } else if (lhsSample.color.equals(Kind.KW_green)) {
            mv.visitInsn(ICONST_2);
        } else if (lhsSample.color.equals(Kind.KW_blue)) {
            mv.visitInsn(ICONST_3);
        }
        mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "updatePixelColor", RuntimeImageSupport.updatePixelColorSig,
                itf);
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        pixelSelector.ex.visit(this, arg);
        pixelSelector.ey.visit(this, arg);
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        // TODO refactor and extend as necessary
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        // cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
        // it is
        // sometime helpful to
        // temporarily run it without COMPUTE_FRAMES. You probably
        // won't get a completely correct classfile, but
        // you will be able to see the code that was
        // generated.
        className = program.progName;
        classDesc = "L" + className + ";";
        String sourceFileName = (String) arg;
        cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
        cw.visitSource(sourceFileName, null);

        // create main method
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        // initialize
        mv.visitCode();

        // add label before first instruction
        Label mainStart = new Label();
        mv.visitLabel(mainStart);

        CodeGenUtils.genLog(DEVEL, mv, "entering main");

        program.block.visit(this, arg);

        // generates code to add string to log
        CodeGenUtils.genLog(DEVEL, mv, "leaving main");

        // adds the required (by the JVM) return statement to main
        mv.visitInsn(RETURN);

        // adds label at end of code
        Label mainEnd = new Label();
        mv.visitLabel(mainEnd);
        mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
        // Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
        // constructor,
        // asm will calculate this itself and the parameters are ignored.
        // If you have trouble with failures in this routine, it may be useful
        // to temporarily change the parameter in the ClassWriter constructor
        // from COMPUTE_FRAMES to 0.
        // The generated classfile will not be correct, but you will at least be
        // able to see what is in it.
        mv.visitMaxs(0, 0);

        // terminate construction of main method
        mv.visitEnd();

        // terminate class construction
        cw.visitEnd();

        // generate classfile as byte array and return
        return cw.toByteArray();
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
        statementAssign.e.visit(this, arg);
        statementAssign.lhs.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
        Label ifLabel = new Label();
        statementIf.guard.visit(this, arg);
        mv.visitJumpInsn(IFEQ, ifLabel);
        statementIf.b.visit(this, arg);
        mv.visitLabel(ifLabel);
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
        mv.visitVarInsn(ALOAD, 0);
        statementInput.e.visit(this, arg);
        mv.visitInsn(AALOAD);
        Type type = Types.getType(statementInput.declaration.type);
        if (type.equals(Type.INTEGER)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", itf);
            mv.visitVarInsn(ISTORE, statementInput.declaration.slotNumber);
        } else if (type.equals(Type.FLOAT)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", itf);
            mv.visitVarInsn(FSTORE, statementInput.declaration.slotNumber);
        } else if (type.equals(Type.BOOLEAN)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", itf);
            mv.visitVarInsn(ISTORE, statementInput.declaration.slotNumber);
        } else if (type.equals(Type.IMAGE)) {
            if (statementInput.declaration.width != null && statementInput.declaration.height != null) {
                mv.visitTypeInsn(NEW, "java/lang/Integer");
                mv.visitInsn(DUP);
                statementInput.declaration.width.visit(this, arg);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V", itf);
                mv.visitTypeInsn(NEW, "java/lang/Integer");
                mv.visitInsn(DUP);
                statementInput.declaration.height.visit(this, arg);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V", itf);
            } else {
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ACONST_NULL);
            }
            mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "readImage",
                    RuntimeImageSupport.readImageSig, itf);
            mv.visitVarInsn(ASTORE, statementInput.declaration.slotNumber);
        } else if (type.equals(Type.FILE)) {
            mv.visitVarInsn(ASTORE, statementInput.declaration.slotNumber);
        }
        return null;
    }

    @Override
    public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
        /**
         *
         * For integers, booleans, and floats, generate code to print to console. For
         * images, generate code to display in a frame.
         *
         * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
         * consuming top of stack.
         */
        statementShow.e.visit(this, arg);
        Type type = statementShow.e.getType();
        switch (type) {
            case INTEGER: {
                CodeGenUtils.genLogTOS(GRADE, mv, type);
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitInsn(Opcodes.SWAP);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", itf);
            }
            break;
            case BOOLEAN: {
                CodeGenUtils.genLogTOS(GRADE, mv, type);
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitInsn(Opcodes.SWAP);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", itf);
            }
            break;
            case FLOAT: {
                CodeGenUtils.genLogTOS(GRADE, mv, type);
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitInsn(Opcodes.SWAP);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", itf);
            }
            break;
            case IMAGE: {
                CodeGenUtils.genLogTOS(GRADE, mv, type);
                mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeFrame",
                        RuntimeImageSupport.makeFrameSig, itf);
                mv.visitInsn(POP);
            }
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
        statementSleep.duration.visit(this, arg);
        mv.visitInsn(I2L);
        // INVOKESTATIC java/lang/Thread.sleep(J)V - J for long
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", itf);
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
        Label whileLabel1 = new Label();
        Label whileLabel2 = new Label();
        mv.visitJumpInsn(GOTO, whileLabel1);
        mv.visitLabel(whileLabel2);
        statementWhile.b.visit(this, arg);
        mv.visitLabel(whileLabel1);
        statementWhile.guard.visit(this, arg);
        mv.visitJumpInsn(IFNE, whileLabel2);
        return null;
    }

    @Override
    public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
        mv.visitVarInsn(ALOAD, statementWrite.sourceDeclaration.slotNumber);
        mv.visitVarInsn(ALOAD, statementWrite.destDeclaration.slotNumber);
        mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "write",
                RuntimeImageSupport.writeSig, itf);
        return null;
    }

}
