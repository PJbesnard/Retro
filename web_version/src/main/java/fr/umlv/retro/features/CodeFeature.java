package fr.umlv.retro.features;

import java.util.Objects;


/**
 * Stocks a feature contained in a class file
 * @author PJBesnard
 *
 */
public class CodeFeature {
	private final String name;
	private final String log;
	private final int classVersion;
	
	/**
	 * creates a new CodeFeature
	 * @param classVersion Java version of the class file which contains the feature
	 * @param name Name of the feature
	 * @param log Log of the feature
	 */
	public CodeFeature(int classVersion, String name, String log) {
		this.classVersion = classVersion;
		this.name = Objects.requireNonNull(name);
		this.log = Objects.requireNonNull(log);
	}

	/**
	 * gets the name of the feature
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * gets the log of the feature
	 * @return
	 */
	public String getLog() {
		return log;
	}
	
	/**
	 * gets the Java version of the Class which contains the feature
	 * @return
	 */
	public int getClassVersion() {
		return classVersion;
	}
	


	
}