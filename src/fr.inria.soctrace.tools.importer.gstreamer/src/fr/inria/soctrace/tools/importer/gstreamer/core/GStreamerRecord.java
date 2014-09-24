/*******************************************************************************
 * Copyright (c) 2012-2014 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.tools.importer.gstreamer.core;

import java.util.HashMap;

/**
 * Class representing a record in a GStreamer trace file.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * @author "Damien Rousseau"
 */
public class GStreamerRecord {
	
	public long timestamp;
	public int pid;
	public String threadId;
	public String typeName;
	public int cpu = 0; // XXX only single CPU traces
	
	// DRO 10/06/2013 - GStreamer generalization
	public HashMap<String, String> attributesValue;
	
	/**
	 * Constructor.
	 * 
	 * Trace file format (default header line):
	 * #timestamp,processID,ThreadID,DebugCategory,UnknowInfo,DebugLevel,source_file,line,function,function:line,object,message
	 * 
	 * @param headerLine header line
	 * @param line trace file line
	 */
	public GStreamerRecord(String headerLine, String line) {
		String[] tokens 		= line.split(GStreamerConstants.DEFAULT_SEPARATOR);
		String[] attributesName = headerLine.split(GStreamerConstants.DEFAULT_SEPARATOR);
		
		timestamp = Long.valueOf(tokens[0]);
		pid = Integer.valueOf(tokens[1]);
		threadId = tokens[2];
	
		// rebuild the whole message and put it in tokens[11], to manage messages containing commas
		StringBuffer buff = new StringBuffer();
		boolean first = true;
		for (int i=11; i<tokens.length; ++i) {
			
			if (!first)
				buff.append(",");
			else 
				first = false;
			
			buff.append(tokens[i]);
		}
		String message = buff.toString();
		tokens[11] = message;

		// Create an attributes array list
		attributesValue = new HashMap<String, String>();
		
		for(int i = 0; i < attributesName.length; i++) {			
			if(i < tokens.length) {
				attributesValue.put(attributesName[i], tokens[i]);				
			}
		}
		
		typeName = tokens[5]+":"+tokens[8]+":"+predicate(message);
	}

	private String predicate(String string) {
		String[] tokens = string.split("\\s+");
		if (tokens.length>0)
			return tokens[0].replaceFirst("(\\d*)(\\D+)(\\d*)(\\s*)(.*)", "$2");
		return "";
	}

	@Override
	public String toString() {
		return "GStreamerRecord [timestamp=" + timestamp + ", pid=" + pid
				+ ", threadId=" + threadId + ", typeName=" + typeName
				+ ", cpu=" + cpu + "]";
	}

}
