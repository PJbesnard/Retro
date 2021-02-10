package fr.umlv.retro.classvisitors.visitors;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;


/**
 * LambdaDetector detector, based on Detector which inherits ClassVisitor
 * @author PJBesnard
 *
 */
public class LambdaVisitor extends FeatureVisitor {


	/* Class infos */
	private String className;
	private String actualMethod;
	private String actualType;
	private int version;



	/**
	 * Stocks the name and the Java version of the class visited, based on ClassVisitor.visit
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.className = name;
		this.version = version;
		super.visit(version, access, name, signature, superName, interfaces);
	};

	/**
	 * Stocks the name and the descriptor of the actual method visited, visits a method with a LambdaMethodVisitor, based on ClassVisitor.visitMethod
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		this.actualMethod = name;
		this.actualType = descriptor;
		return new LambdaMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
	}


	/**
	 * LambdaMethodVisitor detector, based on MethodVisitor
	 * @author PJBesnard
	 *
	 */
	class LambdaMethodVisitor extends MethodVisitor {

		private int line;

		/**
		 * Creates a new LambdaMethodVisitor
		 * @param methodVisitor the MethodVisitor to which delegate the visit of the method
		 */
		public LambdaMethodVisitor(MethodVisitor methodVisitor) {
			super(Opcodes.ASM7, methodVisitor);
		}

		/**
		 * Notifies LambdaDetector observers when visits a lambda instruction, based on MethodVisitor.visitInvokeDynamicInsn
		 */
		@Override
		public void visitInvokeDynamicInsn(java.lang.String name, java.lang.String descriptor,
				Handle bootstrapMethodHandle, java.lang.Object... bootstrapMethodArguments) {

			if (bootstrapMethodHandle.getName().equals("metafactory")) {
				notifyObservers(version, "LAMBDA", "LAMBDA at " + className
						+ "." + actualMethod + actualType + " (" + className + ".java" + ":" + line + "):" + " lambda "
						+ descriptor.substring(descriptor.indexOf('L') + 1, descriptor.length() - 1) + " capture ["
						+ descriptor.substring(descriptor.indexOf('(') + 1, descriptor.indexOf(')')) + "] calling "
						+ bootstrapMethodArguments[1].toString().split(" ")[0]);

			}
			super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
		}

		
		/**
		 * Stocks the line number of the instruction visited, based on MethodVisitor.visitLineNumber
		 */
		@Override
		public void visitLineNumber(int line, Label start) {
			this.line = line;
			super.visitLineNumber(line, start);
		}

	}

}
