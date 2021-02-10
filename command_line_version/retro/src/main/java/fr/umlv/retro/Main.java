package fr.umlv.retro;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import fr.umlv.retro.classvisitors.updaters.VersionUpdater;
import fr.umlv.retro.classvisitors.visitors.ConcatDetector;
import fr.umlv.retro.classvisitors.visitors.Detector;
import fr.umlv.retro.classvisitors.visitors.LambdaDetector;
import fr.umlv.retro.classvisitors.visitors.NestmateDetector;
import fr.umlv.retro.classvisitors.visitors.RecordDetector;
import fr.umlv.retro.classvisitors.visitors.TryWithRessourcesDetector;
import fr.umlv.retro.features.CodeFeature;
import fr.umlv.retro.features.FeaturesStocker;
import fr.umlv.retro.parsers.FileParser;
import fr.umlv.retro.parsers.OptionsParser;

public class Main {

	private String className;
	private ClassReader reader;
	private ClassWriter writer;

	public Main(String className) throws IOException {
		reader = new ClassReader(Files.readAllBytes(Paths.get(className)));
		this.className = className;
		this.writer = new ClassWriter(0);
	}
	
	/**
	 * verify if the target version is correct
	 * @param parser the parser who read the file
	 * @param featuresAvailable the available features 
	 * @param obs the features stocker 
	 */
	private void verifyVersion(OptionsParser parser, Map<String, Integer> featuresAvailable, FeaturesStocker obs) {
		int targetVersion = parser.getTarget();
		if(targetVersion < 5 || targetVersion > 14) {
			throw new IllegalArgumentException("Version " + targetVersion + " not supported");
		}
		for (CodeFeature f : obs.getFeatures()) {
			if(featuresAvailable.get(f.getName()) > targetVersion) {
				throw new IllegalArgumentException("Feature " + f.getName() + " has been implemented in Java " + featuresAvailable.get(f.getName()) + " but you asked " + targetVersion);
			}
		}
	}
	
	/**
	 * show informations about asked features
	 * @param parser the parser who read the file
	 * @param featuresAvailable the available features 
	 * @param obs the features stocker 
	 */
	private void showInfoFeatures(OptionsParser parser, Map<String, Integer> featuresAvailable, FeaturesStocker obs) {
		List<String> features = parser.featuresAsk();
		if(features.size() == 0) {
			features = List.of("LAMBDA", "CONCATENATION", "NESTMATES", "RECORD", "TRY_WITH_RESOURCES");
		}
		for (String s : features) {
			if(featuresAvailable.get(s) == null) {
				throw new IllegalArgumentException("feature " + s + " doesn't exist");
			}
		}
		System.out.println(obs.getFeaturesLog(features));
	}
	
	/**
	 * print the help 
	 */
	private static void printHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("Usage: retro <options> sourcefiles\n\t").append("where possible options include:\n\t")
		.append("-target <release>\n\t Generate class files suitable for the specified Java SE release. Supported releases: 5, 6, 7, 8, 9, 10, 11, 12, 13")
		.append("-features <features>(,<features>)*\n\t target will use only the specified features or all if option not call").append("-info\n\tshow detected features in file");
		System.out.println(sb.toString());
	}
	
	/**
	 * call all options specified by the user
	 * @param parser the parser who read the file
	 * @param featuresAvailable the available features 
	 * @param obs the features stocker 
	 * @throws IOException 
	 */
	public void callOptions(OptionsParser parser, Map<String, Integer> featuresAvailable, FeaturesStocker obs) throws IOException {
		for (String s : parser.optionsAsk()) {
			switch (s) {
			case "-help": printHelp(); break;
			case "-target": verifyVersion(parser, featuresAvailable, obs); writeNewClass(parser); break;
			case "-info": showInfoFeatures(parser, featuresAvailable, obs); break;
			default: break;
			}
		}
	}
	
	/**
	 * Call all the specified observers on the class
	 * @param observers the list of the observers 
	 */
	public void observeClass(List<Detector> observers) {
		var multiTruc = DetectionChain.createDetectionChain(observers);
		reader.accept(multiTruc, 0);
	}
	
	/**
	 * Write a new class with the target version
	 * @param parser the parser who read arguments
	 * @throws IOException if the file can't be write
	 */
	public void writeNewClass(OptionsParser parser) throws IOException {
		Detector d = new VersionUpdater(parser.getTarget());
		d.SetClassVisitor(writer);
		reader.accept(d, 0);
		FileOutputStream os = new FileOutputStream("RetroTarget/" + className);
		os.write(writer.toByteArray());
		os.close();
		System.out.println("Class " + className + " retro-ified successfully, you can check the result in \"RetroTarget\" repertory");
	}
	
	/**
	 * Manage options given in args, with the given parser 
	 * @param args arguments to parse
	 * @param parser the parser which parse the options
	 */
	private static void optionsCall(String[] args, OptionsParser parser) {
		if(args[0].equals("-help") && args.length == 1) {
			printHelp();
			return;
		}
		try {
			parser.checkOptions(args);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Read the file and call the features on it 
	 * @param parser the parser who read arguments
	 * @param observers the observer which contain the detectors
	 * @param featuresAvailable the available features
	 * @param obs the features stocker argument
	 * @throws IOException if the file can't be read
	 */
	private static void fileRead(OptionsParser parser, List<Detector> observers, Map<String, Integer> featuresAvailable, FeaturesStocker obs) throws IOException {
		FileParser fp = new FileParser();
		List<String> files = fp.readFile(parser.getFile());
		for (String f : files) {
			Main classwriter = new Main(f);
			classwriter.observeClass(observers);
			classwriter.callOptions(parser, featuresAvailable, obs);
		}
	}

	public static void main(String[] args) {
		try {
			OptionsParser parser = new OptionsParser();
			optionsCall(args, parser);
			new File("RetroTarget/").mkdirs();
			List<Detector> detectors = List.of(new ConcatDetector(), new LambdaDetector(), new NestmateDetector(), new RecordDetector(), new TryWithRessourcesDetector());
			var obs = new FeaturesStocker();
			detectors.forEach(d -> d.addObserver(obs));
			Map<String, Integer> featuresAvailable = Map.of("LAMBDA", 8, "CONCATENATION", 9, "NESTMATES", 11, "RECORD", 14, "TRY_WITH_RESOURCES", 7);
			List<Detector> observers = new ArrayList<>();
			detectors.forEach(d -> observers.add(d));
			try {
				fileRead(parser, observers, featuresAvailable, obs);
			} catch (IOException e) {
				System.out.println("creation failed, file not found");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}