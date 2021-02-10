package fr.umlv.retro.features;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Observer, had to be used with NeutralVisitor, stocks a list of features contains in a classFile
 * @author PJBesnard
 *
 */
public class FeaturesStocker implements FeaturesObserver {
	private final List<CodeFeature> features = new ArrayList<>();

	/**
	 * Stocks in features a new feature detected
	 */
	@Override
	public void onFeatureDetected(int classVersion, String featureName, String log) {
		features.add(new CodeFeature(classVersion, featureName, log));
	}

	/**
	 * Get all features's logs detected
	 * @return a String version of all features's logs detected
	 */
	public String getAllFeaturesLog() {
		var sj = new StringJoiner("\n");
		features.forEach(feature -> sj.add(feature.getLog()));
		return sj.toString();
	}
	
	public String getFeaturesLog(List<String> features) {
		var sj = new StringJoiner("\n");
		features.forEach(name -> {
			this.features.forEach(feature -> {
				if (feature.getName().equals(name)) {
					sj.add(feature.getLog());
				}
			});
		});
		return sj.toString();
	}

	/**
	 * Get all features detected
	 * @return a List version of all features detected
	 */
	public List<CodeFeature> getFeatures() {
		return features;
	}
}
