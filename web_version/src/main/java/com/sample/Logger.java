package com.sample;

import java.util.StringJoiner;

public class Logger {
	private StringJoiner messages = new StringJoiner("\n");
	
	public void log(String message) {
		messages.add(message);
	}
	
	public String getLog() {
		return messages.toString();
	}
}
