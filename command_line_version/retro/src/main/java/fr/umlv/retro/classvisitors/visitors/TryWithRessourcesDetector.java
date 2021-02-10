package fr.umlv.retro.classvisitors.visitors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * TryWithRessourcesDetector detector, based on Detector which inherits ClassVisitor
 * @author LBillaut
 *
 */
public class TryWithRessourcesDetector extends Detector {
	

	private String className;
	private int version;
	
	/**
	 * Stocks the name and the Java version of the class visited, based on ClassVisitor.visit
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		className = name;
		this.version = version;
		super.visit(version, access, name, signature, superName, interfaces);
	};
	
	/**
	 * Visits a method with a LambdaMethodVisitor, based on ClassVisitor.visitMethod
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		return new TryWithResourcesMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions), name, descriptor);
	}
	
	/**
	 * TryWithResourcesMethodVisitor detector, based on MethodVisitor
	 * @author LBillaut
	 *
	 */
	class TryWithResourcesMethodVisitor extends MethodVisitor {
		private final TryWithResourcesFinder finder = new TryWithResourcesFinder();
		private final String methodName;
		private final String methodDescriptor;
		private Label start;
		
		/**
		 * Creates a new TryWithResourcesMethodVisitor
		 * @param mv MethodVisitor to which delegate the visit of the method
		 * @param methodName Name of the visited method
		 * @param methodDescriptor Description of the visited method
		 */
		public TryWithResourcesMethodVisitor(MethodVisitor mv, String methodName, String methodDescriptor) {
			super(Opcodes.ASM7, mv);
			this.methodName = methodName;
			this.methodDescriptor = methodDescriptor;
		}
		
		/**
		 * Stocks the visited label, based on ClassVisitor.visitLabel
		 */
		@Override
		public void visitLabel(Label label) {
			finder.testLabel(label);
			super.visitLabel(label);
		}
		
		/**
		 * Stocks the label of the jump instruction method, based on ClassVisitor.visitJumpInsn
		 */
		@Override
		public void visitJumpInsn(int opcode, Label label) {
			if(opcode == 167) {
				finder.addEndCatchLabel(label);
			}
			super.visitJumpInsn(opcode, label);
		}
		
		/**
		 * Stocks the start and end labels and handler of the try-catch-block visited, based on ClassVisitor.visitTryCatchBlock
		 */
		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			finder.addCatchAndFinallyLabels(end, handler);
			this.start = start;
			super.visitTryCatchBlock(start, end, handler, type);
		}
		
		/**
		 * Notifies TryWithResourcesMethodVisitor observers when detects try-with-ressources
		 */
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
			if(finder.testIfTwr(opcode, name, owner)) {
				notifyObservers(version, "TRY_WITH_RESOURCES", "TRY_WITH_RESOURCES at " + className + "." + methodName + methodDescriptor + " (" + className + ".java:" + finder.getBeginningLine() + ") " + "try-with-resources on " + finder.getOwner());
			}
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		}
		
		/**
		 * Stocks the line number of the try-with-ressources instruction
		 */
		@Override
		public void visitLineNumber(int line, Label start) {
			if(this.start != null) {
				if(this.start.equals(start)) {
					finder.addStartLine(line);
				}
			}
			super.visitLineNumber(line, start);
		}
	}
	
	/**
	 * TryCatch analyser, could detect a TryWithRessources block
	 * @author LBillaut
	 *
	 */
	class TryWithResourcesFinder {
		private Map<Label, Label> catchMap = new HashMap<Label, Label>();
		private Map<Label, Label> finallyMap = new HashMap<Label, Label>();
		
		private LinkedList<Label> catchList = new LinkedList<Label>();
		
		private boolean inCatchBlock = false;
		private boolean inFinallyBlock = false;
		
		private Label catchEnd = null;
		private Label finallyEnd = null;
		
		private boolean firstCloseDetected = false;
		private boolean secondCloseDetected = false;
		private boolean addSuppressedDetected = false;
		
		private String owner;
		private int line = 0;
		
		/**
		 * Test if the label is a start of a catch or an end of a catch
		 * @param label the label to which be test
		 */
		private void testCatchLabel(Label label) {
			if(catchMap.containsKey(label)) { //debut du catch
				inCatchBlock = true;
				catchEnd = (Label) catchMap.get(label);
			}
			if(label.equals(catchEnd)) { // fin du catch
				inCatchBlock = false;
			}
		}
		
		/**
		 * Test if the label is a start of a catch or an end of a catch
		 * @param label the label to which be test
		 */
		private void testFinallyLabel(Label label) {
			if(finallyMap.containsKey(label)) { //debut de finally
				inFinallyBlock = true;
				finallyEnd = (Label) finallyMap.get(label);
			}
			if(label.equals(finallyEnd)) { //fin de finally
				inFinallyBlock = false;
			}
		}
		
		/**
		 * Test if the label is a start of a catch / or a finally or an end of a catch / or a finally
		 * @param label the label to which be test
		 */
		public void testLabel(Label label) {
			Objects.requireNonNull(label);
			testCatchLabel(label);
			testFinallyLabel(label);
		}
		
		/**
		 * Add the start label and the end label to the catchMap
		 * @param label the label to which represent the end of the catch 
		 */
		public void addEndCatchLabel(Label label) {
			Objects.requireNonNull(label);
			if(catchList != null && catchList.size() > 0) {
				catchMap.put(catchList.removeFirst(), label);
			}
		}
		
		/**
		 * Add the start label and the end label to the finallyMap and the catchList
		 * @param end the label to which represent the start of the finally
		 * @param handler the label to which represent the end of the finally and the start of the catch
		 */
		public void addCatchAndFinallyLabels(Label end, Label handler) {
			finallyMap.put(end, handler);
			catchList.add(handler); 
		}
		
		/**
		 * Test if the instruction is a close in a finally block
		 * @param name the name of the instruction to which be test
		 * @param owner the name of the owner class of the class to which be test
		 */
		private void detectFirstClose(String name, String owner) {
			if(name.equals("close") && inFinallyBlock) {
				firstCloseDetected = true;
				this.owner = owner;
			}
		}
		
		/**
		 * Test if the instruction is a close in a catch block
		 * @param name the name of the instruction to which be test
		 */
		private void detectSecondClose(String name) {
			if(name.equals("close") && inCatchBlock) {
				secondCloseDetected = true;
			}
		}
		
		/**
		 * Test if the instruction is an addSuppressed in a catch block
		 * @param opcode the opcode of the instruction to which be test
		 * @param name the name of the instruction to which be test
		 * @param owner the owner class name of the instruction to which be test
		 */
		private void detectAddSuppressed(int opcode, String name, String owner) {
			if (opcode == Opcodes.INVOKEVIRTUAL && name.equals("addSuppressed") && owner.equals("java/lang/Throwable") && inCatchBlock) {
				addSuppressedDetected = true;
			}
		}
		
		/**
		 * Detect a Try-With-Resources 
		 * @return True if a Try-With-Resources is detected, False either
		 */
		private boolean detectTwr() {
			if(addSuppressedDetected && firstCloseDetected && secondCloseDetected) {
				addSuppressedDetected = false;
				firstCloseDetected = false;
				secondCloseDetected = false;
				return true;
			}
			return false;
		}
		
		/**
		 * Test if a Try-With-Resources is present
		 * @param opcode the opcode of the instruction
		 * @param name the name of the instruction
		 * @param owner the owner class of the instruction a<
		 * @return True if a Try-With-Resources is detected, False either
		 */
		public boolean testIfTwr(int opcode, String name, String owner) {
			detectFirstClose(name, owner);
			detectSecondClose(name);
			detectAddSuppressed(opcode, name, owner);
			return detectTwr();
		}
		
		/**
		 * Return the owner class of the instruction in the Try-With-Resources
		 * @return the owner class of the instruction in the Try-With-Resources
		 */
		public String getOwner() {
			return owner;
		}
		
		/**
		 * Save the starting line of the Try-With-Resources
		 * @param line the line to save
		 */
		public void addStartLine(int line) {
			this.line = line;
		}
		
		/**
		 * Return the line of beginning of the Try-With-Resources
		 * @return the line of beginning of the Try-With-Resources
		 */
		public int getBeginningLine() {
			return line - 1;
		}
		
	}
	
	
	
}