package fr.umlv.retro.classvisitors.visitors;

import java.util.LinkedList;
import java.util.StringJoiner;

/**
 * NestmateDetector detector, based on Detector which inherits ClassVisitor
 * 
 * @author LBillaut
 *
 */
public class NestmateVisitor extends FeatureVisitor {

	private int version;
	private String name;
	private LinkedList<String> nestMemberList = new LinkedList<String>();
	private String nestHost;

	/**
	 * Stocks the name and the Java version of the class visited, based on ClassVisitor.visit
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.name = name;
		this.version = version;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	/**
	 * Stocks the nestmember visited, based on ClassVisitor.visitNestMember
	 */
	@Override
	public void visitNestMember(final String nestMember) {
		nestMemberList.addFirst(nestMember);
		super.visitNestMember(nestMember);
	}

	/**
	 * Stocks the nestHost visited, based on ClassVisitor.visitNestMember
	 */
	@Override
	public void visitNestHost(final String nestHost) {
		this.nestHost = nestHost;
		super.visitNestMember(nestHost);
	}

	/**
	 * Creates message for observers when a nesthost is detected in class
	 * @return a String which represents the log of the feature detected
	 */
	private String messageForEmptyList() {
		return "NESTMATES at " + name + " (" + nestHost + ".java): nestmate of " + nestHost;
	}

	/**
	 * Creates message for observers when a nestmember is detected in class
	 * @return a String which represents the log of the feature detected
	 */
	private String messageForNotEmptyList() {
		StringJoiner sb = new StringJoiner(", ", "[", "]");
		nestMemberList.forEach(nm -> sb.add(nm));
		return "NESTMATES at " + name + " (" + name + ".java): new host " + name + " members " + sb.toString();
	}

	/**
	 * Notifies observers  of detected nesthosts and nestmembers, based on ClassVisitor.visitEnd
	 */
	@Override
	public void visitEnd() {
		if (nestMemberList.size() == 0 && nestHost != null) {
			notifyObservers(version, "NESTMATES", messageForEmptyList());
		} else if (nestMemberList.size() > 0) {
			notifyObservers(version, "NESTMATES", messageForNotEmptyList());
		}
		super.visitEnd();
	}
}
