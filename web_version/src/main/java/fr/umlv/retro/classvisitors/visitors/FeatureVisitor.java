package fr.umlv.retro.classvisitors.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import fr.umlv.retro.features.FeaturesObserver;

/**
 * Detector which had to be inherits by another Class to be used has a
 * ClassVisitor which implements a list of FeaturesObserver interface
 * 
 * @author PJBesnard
 *
 */
public class FeatureVisitor extends ClassVisitor {
	private final List<FeaturesObserver> observers = new ArrayList<>();

	/**
	 * Create a new instance of Detector
	 */
	public FeatureVisitor() {
		super(Opcodes.ASM7);
	}

	/**
	 * Sets the ClassVisitor of the Detector
	 * 
	 * @param cv the ClassVisitor
	 */
	public void SetClassVisitor(ClassVisitor cv) {
		super.cv = cv;
	}

	/**
	 * Add a new FeaturesObserver to the Detector
	 * 
	 * @param featuresObserver
	 */
	public void addObserver(FeaturesObserver featuresObserver) {
		Objects.requireNonNull(featuresObserver);
		this.observers.add(featuresObserver);
	}

	/**
	 * Add (ClassFile version, FeatureName, FeatureLog) to all Detector observer
	 * 
	 * @param version of the Class file which contains feature
	 * @param feature name of the feature
	 * @param log log of the feature
	 */
	public void notifyObservers(int version, String feature, String log) {
		observers.forEach(observer -> observer.onFeatureDetected(version, feature, log));
	}

}
