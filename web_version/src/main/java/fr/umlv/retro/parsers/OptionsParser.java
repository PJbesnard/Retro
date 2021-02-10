package fr.umlv.retro.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Parse arguments
 * @author LBillaut
 *
 */
public class OptionsParser {

	private final HashMap<String, Integer> optionsAvailables = new HashMap<String, Integer>(); 
	private final ArrayList<String> features = new ArrayList<String>();
	private int target;
	private boolean dontCheck = false;
	private String file;
	
	/**
	 * Create a parser
	 */
	public OptionsParser() {
		optionsAvailables.put("-help", 0);
		optionsAvailables.put("-info", 0);
		optionsAvailables.put("-target", 0);
		optionsAvailables.put("-features", 0);
	}
	
	/**
	 * Check if the given option have a dash
	 * @param s the given option
	 */
	private void checkIfOptionHaveDash(String s) throws IllegalArgumentException {
		if(!s.contains("-") || !s.startsWith("-", 0)) {
			throw new IllegalArgumentException("Option " + s + " must start with a -");
		}
	}
	
	/**
	 * Check if the given option exist
	 * @param s the option
	 */
	private void checkIfOptionExist(String s) throws IllegalArgumentException {
		if(!optionsAvailables.containsKey(s)) {
			throw new IllegalArgumentException("Option " + s + " doesn't exist");
		}
	}
	
	/**
	 * Check if the option -features is correct
	 * @param i actual index
	 * @param args list of given arguments
	 */
	private void checkFeatures(int i, String[] args) throws IllegalArgumentException {
		if(!args[i].equals("-features")) {
			return;
		}
		if(i >= args.length - 2 || optionsAvailables.containsKey(args[i + 1])) {
			throw new IllegalArgumentException("-features need features write like: feature1,feature2,feature3");
		}
		for (String s : args[i + 1].split(",")) {
			features.add(s.toUpperCase());
		}
		dontCheck = true;
	}
	
	/**
	 * Check if the version of option -target is correct
	 * @param i actual index
	 * @param args list of given arguments
	 */
	private void checkVersionTarget(int i, String[] args) {
		try {
			target = Integer.parseInt(args[i + 1]);
		} catch (Exception e) {
			throw new IllegalArgumentException("-target need a target");
		}
		dontCheck = true;
	}
	
	/**
	 * Check if the option -target is correct
	 * @param i actual index
	 * @param args list of given arguments
	 */
	private void checkTarget(int i, String[] args) throws IllegalArgumentException {
		if(!args[i].equals("-target")) {
			return;
		}
		if(i >= args.length - 2 || optionsAvailables.containsKey(args[i + 1])) {
			throw new IllegalArgumentException("-target need a target");
		}
		checkVersionTarget(i, args);
	}
	
	/**
	 * Check validity of givens options
	 * @param i actual index
	 * @param args list of given arguments
	 */
	private void checkForValidOptions(int i, String[] args) throws IllegalArgumentException {
		checkFeatures(i, args);
		checkTarget(i, args);
		checkIfOptionHaveDash(args[i]);
		checkIfOptionExist(args[i]);
		if(optionsAvailables.get(args[i]) > 0) {
			throw new IllegalArgumentException("Option " + args[i] + " already used");
		}
		optionsAvailables.put(args[i], 1);
	}
	

	
	/**
	 * Check givens options
	 * @param args list of given arguments
	 */
	public void checkOptions(String[] args) {
		for(int i = 0; i < args.length - 1; i++) {
			System.out.println("option: " + args[i]);
			if(dontCheck) {
				dontCheck = false;
				continue;
			}
			checkForValidOptions(i, args);
		}
		file = args[args.length - 1];
	}
	
	/**
	 * Return the target version
	 * @return the target version
	 */
	public int getTarget() {
		return target;
	}
	
	/**
	 * Return the features ask by the user
	 * @return the features ask by the user
	 */
	public List<String> featuresAsk() {
		return features;
	}
	
	/**
	 * Return the features ask by the user
	 * @return the features ask by the user
	 */
	public List<String> optionsAsk() {
		List<String> options = new ArrayList<String>();
		for (String s : optionsAvailables.keySet()) {
			if(optionsAvailables.get(s) > 0) {
				options.add(s);
			}
		}
		return options;
	}
	
	public String getFile() {
		return file;
	}
}
