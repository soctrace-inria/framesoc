/**
 * 
 */
package fr.inria.soctrace.framesoc.core.tools.model;

import java.util.List;

/**
 * Default input for importers not extending the extension point for the tool input.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FileInput implements IFramesocToolInput {

	private List<String> files;

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	@Override
	public String getCommand() {
		return "";
	}

}
