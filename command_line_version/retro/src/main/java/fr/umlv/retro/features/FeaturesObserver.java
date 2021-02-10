package fr.umlv.retro.features;

/**
 * Observer which stocks features detected
 * @author PJBesnard
 *
 */
public interface FeaturesObserver {
	
	/**
	 * Do things when a new feature is added
	 * @param classVersion Version of the class file
	 * @param featureName Name of the feature
	 * @param log Log of the feature
	 */
	public void onFeatureDetected(int classVersion, String featureName, String log);
	
}
