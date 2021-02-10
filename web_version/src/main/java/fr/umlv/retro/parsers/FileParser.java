package fr.umlv.retro.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Parse a file directory
 * @author LBillaut
 *
 */
public class FileParser {
	private final List<String> files = new ArrayList<String>();
	
	/**
	 * Add a class file to the file list
	 * @param s the .class name
	 */
	private void addClass(String s) {
		files.add(s);
	}
	
	/**
	 * Add a class file of a Jar to the file list
	 * @param jarFile the owner jar 
	 * @param entry the owner jar entry
	 * @param s the owner jar name
	 * @throws IOException if the jar file can't be open
	 */
	private void addClassOfJar(JarFile jarFile, JarEntry entry, String s) throws IOException {
		if (entry.getName().endsWith(".class")) {
			try (InputStream inputStream = jarFile.getInputStream(entry)) {
				files.add(s + "/" + entry.getName());
			} catch (IOException ioException) {
				System.out.println("Invalid entry for " + entry.getName());
				throw ioException;
			}
		}
	}
	
	/**
	 * Read a Jar file and add all his .class in the file list
	 * @param s the owner jar name
	 * @throws IOException if the jar file can't be open
	 */
	private void readAndAddJar(String s) throws IOException {
		try (JarFile jarFile = new JarFile(s)) {
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				addClassOfJar(jarFile, entry, s);			
			}
		}
	}
	
	/**
	 * Add all .class of a file to the files list
	 * @param listOfFiles the list of files
	 * @param s the owner file name
	 */
	private void addClassOfFile(File[] listOfFiles, String s) {
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	if(file.getName().endsWith(".class")) {
		    		files.add(s + "/" + file.getName());
		    	}
		    }
		}
	}
	
	/**
	 * Read all the files in a directory
	 * @param s the owner file name
	 */
	private void readAndAddFile(String s) {
		File folder = new File(s);
		File[] listOfFiles = folder.listFiles();
		if(listOfFiles == null) {
			throw new IllegalArgumentException(s + " is not a valid file");
		}
		addClassOfFile(listOfFiles, s);
	}
	
	/**
	 * Read a file and add all .class to the files list
	 * @param s the owner file name
	 * @throws IOException if an error is encountered while opening a file 
	 */
	public List<String> readFile(String s) throws IOException {
		if (s == null) { return files;}
		if(s.endsWith(".class")) {
			addClass(s);
		}
		else if(s.endsWith(".jar")) {
			readAndAddJar(s);
		}
		else {
			readAndAddFile(s);
		}
		return files;
	}
}
