/**
 * 
 */
package fr.inria.soctrace.framesoc.core.tools.model;

import java.util.List;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceFileInput implements IFramesocToolInput {

	private List<String> traceFiles;

	public List<String> getTraceFiles() {
		return traceFiles;
	}

	public void setTraceFiles(List<String> traceFiles) {
		this.traceFiles = traceFiles;
	}

	@Override
	public String getCommand() {
		return "";
	}
	
}
