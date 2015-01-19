/**
 * 
 */
package fr.inria.soctrace.framesoc.core.tools.model;

import java.util.List;

/**
 * Default input for importers not extending the extension point for the tool input.
 * 
 * If an importer tool does not provide an extension for the extension point
 * fr.inria.soctrace.framesoc.ui.input.toolInput, this is the type of input that it will receive
 * when launched by Framesoc.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FileInput extends EmptyInput {

	private List<String> files;

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

}
