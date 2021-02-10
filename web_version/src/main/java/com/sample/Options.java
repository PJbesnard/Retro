package com.sample;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import fr.umlv.retro.Main;
import com.sample.Logger;

 
@Path("/options")
public class Options {
 
    //private Set<String> options = Collections.newSetFromMap(Collections.synchronizedMap(new LinkedHashMap<>()));
	private String options = "";
	private String[] message = {""}; 
 
    
    //@Produces(MediaType.TEXT_HTML)
	@GET
    public String[] list() {
		for (String m : message) {
			System.out.println(m);
		}
		return message;
    }
 
    
    //@Produces(MediaType.TEXT_HTML)
	@POST
    public String[] add(String option) {
		System.out.println(option);
    	String[] args = option.replaceAll("\"", "").split(" ");
    	Logger logger = new Logger();
    	Main.start(args, logger);
    	message = logger.getLog().split("\n");
    	for (String m : message) {
			System.out.println("wesh: " + m);
		}
    	return message;
    }
 
}