package fr.umlv.retro.classvisitors.updaters;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import fr.umlv.retro.classvisitors.visitors.FeatureVisitor;

/**
 * Met à jour la version du fichier
 * @author LBillaut
 *
 */
public class VersionUpdater extends FeatureVisitor {
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
