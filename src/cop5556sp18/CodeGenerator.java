/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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

package cop5556sp18;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

import cop5556sp18.CodeGenUtils;
import cop5556sp18.Scanner.Kind;

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

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	final boolean itf = false;

	private Integer slotNumCounter;

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
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
		Label decLabel = new Label();
		if (Types.getType(declaration.type) == Type.IMAGE) {
			mv.visitLabel(decLabel);
			if (declaration.width == null && declaration.height == null) {
				mv.visitLdcInsn(defaultWidth);
				mv.visitLdcInsn(defaultHeight);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage",
						RuntimeImageSupport.makeImageSig, itf);
			} else {
				declaration.width.visit(this, arg);
				declaration.height.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage",
						RuntimeImageSupport.makeImageSig, itf);
			}
			mv.visitVarInsn(ASTORE, declaration.slotNumber);
		} else if (Types.getType(declaration.type) == Type.INTEGER) {
			mv.visitLdcInsn(new Integer(0));
			mv.visitVarInsn(ISTORE, declaration.slotNumber);
		} else if (Types.getType(declaration.type) == Type.FLOAT) {
			mv.visitLdcInsn(new Float(0));
			mv.visitVarInsn(FSTORE, declaration.slotNumber);
		} else if (Types.getType(declaration.type) == Type.BOOLEAN) {
			mv.visitLdcInsn(new Boolean(false));
			mv.visitVarInsn(ISTORE, declaration.slotNumber);
		} else if (Types.getType(declaration.type) == Type.FILE) {
			mv.visitLdcInsn(new String());
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
		// TODO: not done yet
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
		default:
		}
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
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
		} else if(fxn.equals(Scanner.Kind.KW_float)) {
			 if (type.equals(Type.INTEGER)) {
				 mv.visitInsn(I2F);
			 }
			
		} else if(fxn.equals(Scanner.Kind.KW_int)) {
			System.out.println("f2i");
			
			if (type.equals(Type.FLOAT)) {
				mv.visitInsn(F2I);
			}
			
		} else if(fxn.equals(Scanner.Kind.KW_width)) {
			if(type.equals(Type.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getWidth", RuntimeImageSupport.getWidthSig,
						itf);
			}
			
		} else if(fxn.equals(Scanner.Kind.KW_height)) {
			if(type.equals(Type.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getHeight", RuntimeImageSupport.getHeightSig,
						itf);
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO: check if correct
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
			// TODO: check what to do here
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	// TODO: check if done completely
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

	// TODO: check resize issue
	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
//		Type type = Types.getType(statementAssign.lhs.declaration.type);
//		if (type == Type.INTEGER) {
//			mv.visitVarInsn(ISTORE, statementAssign.lhs.declaration.slotNumber);
//		} else if (type == Type.FLOAT) {
//			mv.visitVarInsn(FSTORE, statementAssign.lhs.declaration.slotNumber);
//		} else if (type == Type.BOOLEAN) {
//			mv.visitVarInsn(ISTORE, statementAssign.lhs.declaration.slotNumber);
//		} else if (type == Type.IMAGE) {
//			mv.visitVarInsn(ASTORE, statementAssign.lhs.declaration.slotNumber);
//		} else if (type == Type.FILE) {
//			mv.visitVarInsn(ASTORE, statementAssign.lhs.declaration.slotNumber);
//		}
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		statementInput.e.visit(this, arg);
		mv.visitInsn(AALOAD);
		Type type = Types.getType(statementInput.declaration.type);
		if (type == Type.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", itf);
			mv.visitVarInsn(ISTORE, statementInput.declaration.slotNumber);
		} else if (type == Type.FLOAT) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", itf);
			mv.visitVarInsn(FSTORE, statementInput.declaration.slotNumber);
		} else if (type == Type.BOOLEAN) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", itf);
			mv.visitVarInsn(ISTORE, statementInput.declaration.slotNumber);
		} else if (type == Type.IMAGE) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "readImage",
					RuntimeImageSupport.readImageSig, itf);
			if (statementInput.declaration.width != null && statementInput.declaration.height != null) {
				statementInput.declaration.width.visit(this, arg);
				statementInput.declaration.height.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "resize",
						RuntimeImageSupport.resizeImageSig, itf);
			}
			mv.visitVarInsn(ASTORE, statementInput.declaration.slotNumber);
		} else if (type == Type.FILE) {
			mv.visitVarInsn(ASTORE, statementInput.declaration.slotNumber);
		}
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		/**
		 * TODO refactor and complete implementation.
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
		}
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	private String getJVMType(Type type) {
		if (type.equals(Type.INTEGER))
			return "I";
		if (type.equals(Type.FLOAT))
			return "F";
		if (type.equals(Type.BOOLEAN))
			return "Z";
		if (type.equals(Type.IMAGE))
			return "Ljava/awt/image/BufferedImage;";
		if (type.equals(Type.FILE))
			return "Ljava/lang/String;";

		return "";
	}

}
