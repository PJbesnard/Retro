package fr.umlv.retro.classvisitors.updaters;

import fr.umlv.retro.classvisitors.visitors.Detector;

/**
 * Met à jour la version du fichier
 * @author LBillaut
 *
 */
public class VersionUpdater extends Detector {
	private int targetVersion;
	
	/**
	 * Create a version Updater
	 * @param targetVersion the target version 
	 */
	public VersionUpdater(int targetVersion) {
		super();
		this.targetVersion = targetVersion;
	}
	
	/**
	 * Change the version based on ClassVisitor.visit
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(44 + targetVersion, access, name, signature, superName, interfaces);
	}
}
