package fr.umlv.retro;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.objectweb.asm.ClassVisitor;

import fr.umlv.retro.classvisitors.visitors.FeatureVisitor;

/**
 * Creates a chain of Detection with static method createTransformationChain
 * @author PJBesnard
 *
 */
public class VisitorChain {
	
	/**
	 * Creates a chain of Detection
	 * @param detectors the list of the detectors which compose transformation chain
	 * @return the first Detector
	 */
	public static ClassVisitor createDetectionChain(List<FeatureVisitor> detectors) {
		Objects.requireNonNull(detectors);
		if (detectors.size() < 1) {
			throw new IllegalArgumentException("List size of detectors can't be less than 0");
		}
		var actual = detectors.get(0);
		for (int i = 1; i < detectors.size(); i++) {
			FeatureVisitor actualDetector = detectors.get(i);
			actualDetector.SetClassVisitor(actual);
			actual = actualDetector;
		}
		return actual;
	}
}
