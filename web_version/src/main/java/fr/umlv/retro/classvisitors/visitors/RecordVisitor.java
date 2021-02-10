package fr.umlv.retro.classvisitors.visitors;




/**
 * RecordDetector detector, based on Detector which inherits ClassVisitor
 * @author LBillaut
 *
 */
public class RecordVisitor extends FeatureVisitor {

	/**
	 * Stocks the name and the Java version of the class visited, based on ClassVisitor.visit
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (superName.equals("java/lang/Record")) {
			super.notifyObservers(version, "RECORD", "RECORD at " + name + " " + name + ".java" + " is a Record Class");
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}


}
