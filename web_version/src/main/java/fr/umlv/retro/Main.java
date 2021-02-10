package fr.umlv.retro;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import com.sample.Logger;

import fr.umlv.retro.classvisitors.updaters.VersionUpdater;
import fr.umlv.retro.classvisitors.visitors.ConcatVisitor;
import fr.umlv.retro.classvisitors.visitors.FeatureVisitor;
import fr.umlv.retro.classvisitors.visitors.LambdaVisitor;
import fr.umlv.retro.classvisitors.visitors.NestmateVisitor;
import fr.umlv.retro.classvisitors.visitors.RecordVisitor;
import fr.umlv.retro.classvisitors.visitors.TryWithRessourcesVisitor;
import fr.umlv.retro.features.CodeFeature;
import fr.umlv.retro.features.FeaturesStocker;
import fr.umlv.retro.parsers.FileParser;
import fr.umlv.retro.parsers.OptionsParser;

public class Main {

	private String className;
	private ClassReader reader;
	private ClassWriter writer;
	
	private Logger logger;
	
	public Main(String className, Logger logger) throws IOException {
		reader = new ClassReader(Files.readAllBytes(Paths.get(className)));
		this.className = className;
		this.writer = new ClassWriter(0);
		this.logger = logger;
	}
	
	/**
	 * verify if the target version is correct
	 * @param parser the parser who read the file
	 * @param featuresAvailable the available features 
	 * @param obs the features stocker 
	 */
	private void verifyVersion(OptionsParser parser, Map<String, Integer> featuresAvailable, FeaturesStocker obs) throws IllegalArgumentException {
		int targetVersion = parser.getTarget();
		if(targetVersion < 5 || targetVersion > 14) {
			throw new IllegalArgumentException("Version " + targetVersion + " not supported");
		}
		for (CodeFeature f : obs.getFeatures()) {
			if(featuresAvailable.get(f.getName()) > targetVersion) {
				logger.log("Feature " + f.getName() + " has been implemented in Java " + featuresAvailable.get(f.getName()) + " but you asked " + targetVersion + "\n");
				throw new IllegalArgumentException();
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
		for (String s : features) {
			if(featuresAvailable.get(s) == null) {
				throw new IllegalArgumentException("feature " + s + " doesn't exist");
			}
		}
		if(features.size() == 0)
			features = List.of("LAMBDA", "CONCATENATION", "NESTMATES", "RECORD", "TRY_WITH_RESOURCES");
		logger.log(obs.getFeaturesLog(features));
	}
	
	/**
	 * print the help 
	 */
	private static void printHelp(Logger logger) {
		StringBuilder sb = new StringBuilder();
		sb.append("Usage: retro <options> sourcefiles\n").append("where possible options include:\n")
		.append("-target <release>    Generate class files suitable for the specified Java SE release. Supported releases: 5, 6, 7, 8, 9, 10, 11, 12, 13\n")
		.append("-features <features>(,<features>)*    target will use only the specified features or all if option not call\n").append("-info   show detected features in file");
		logger.log(sb.toString());
	}
	
	/**
	 * call all options specified by the user
	 * @param parser the parser who read the file
	 * @param featuresAvailable the available features 
	 * @param obs the features stocker 
	 * @throws IOException 
	 */
	public void callOptions(OptionsParser parser, Map<String, Integer> featuresAvailable, FeaturesStocker obs) throws IllegalArgumentException, IOException {
		for (String s : parser.optionsAsk()) {
			switch (s) {
			case "-help": printHelp(logger); break;
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
	public void observeClass(List<FeatureVisitor> observers) {
		var multiTruc = VisitorChain.createDetectionChain(observers);
		reader.accept(multiTruc, 0);
	}
	
	/**
	 * Write a new class with the target version
	 * @param parser the parser who read arguments
	 * @throws IOException if the file can't be write
	 */
	public void writeNewClass(OptionsParser parser) throws IOException {
		FeatureVisitor d = new VersionUpdater(parser.getTarget());
		d.SetClassVisitor(writer);
		reader.accept(d, 0);
		System.out.println(className.replace("../", ""));
		FileOutputStream os = new FileOutputStream("../RetroTarget/" + className.replace("../input/", ""));
		os.write(writer.toByteArray());
		os.close();
		logger.log("Class " + className.replace("../input/", "") + " retro-ified successfully, you can check the result in \"RetroTarget\" repertory");
	}
	
	public static void start(String[] args, Logger logger) {
		OptionsParser parser = new OptionsParser();
		if(args[0].equals("-help")) {
			Main.printHelp(logger);
			return;
		}
		try {
			parser.checkOptions(args);
		} catch (Exception e) {
			logger.log(e.getMessage());
		}
		new File("../RetroTarget/").mkdirs();
		try {
			List<FeatureVisitor> detectors = List.of(new ConcatVisitor(), new LambdaVisitor(), new NestmateVisitor(), new RecordVisitor(), new TryWithRessourcesVisitor());
			var obs = new FeaturesStocker();
			detectors.forEach(d -> d.addObserver(obs));
			Map<String, Integer> featuresAvailable = Map.of("LAMBDA", 8, "CONCATENATION", 9, "NESTMATES", 11, "RECORD", 14, "TRY_WITH_RESOURCES", 7);
			List<FeatureVisitor> observers = new ArrayList<>();
			detectors.forEach(d -> observers.add(d));
			FileParser fp = new FileParser();
			List<String> files; 
			try {
				files = fp.readFile("../input/" + parser.getFile());
			} catch (Exception e) {
				logger.log(e.getMessage());
				return;
			}
			for (String f : files) {
				Main classwriter = new Main(f, logger);
				classwriter.observeClass(observers);
				try {
					classwriter.callOptions(parser, featuresAvailable, obs);
				} catch (Exception e) {
					return;
				}
			}
		} catch (IOException e) {
			logger.log("Creation failed, file not found");
		}
	}
}