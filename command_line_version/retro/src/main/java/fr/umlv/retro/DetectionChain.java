package fr.umlv.retro;

import java.util.List;
import java.util.Objects;

import org.objectweb.asm.ClassVisitor;

import fr.umlv.retro.classvisitors.visitors.Detector;

/**
 * Creates a chain of Detection with static method createTransformationChain
 * @author PJBesnard
 *
 */
public class DetectionChain {
	
	/**
	 * Creates a chain of Detection
	 * @param detectors the list of the detectors which compose transformation chain
	 * @return the first Detector
	 */
	public static ClassVisitor createDetectionChain(List<Detector> detectors) {
		Objects.requireNonNull(detectors);
		if (detectors.size() < 1) {
			throw new IllegalArgumentException("List size of detectors can't be less than 0");
		}
		var actual = detectors.get(0);
		for (int i = 1; i < detectors.size(); i++) {
			Detector actualDetector = detectors.get(i);
			actualDetector.SetClassVisitor(actual);
			actual = actualDetector;
		}
		return actual;
	}
}
