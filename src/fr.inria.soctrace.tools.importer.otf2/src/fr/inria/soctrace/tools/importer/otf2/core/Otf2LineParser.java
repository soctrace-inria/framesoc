package fr.inria.soctrace.tools.importer.otf2.core;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

public interface Otf2LineParser {
	
	/**
	 * Parse the line
	 * @param String containing the text line
	 * @throws SoCTraceException 
	 */
	void parseLine(String type, String fields) throws SoCTraceException;
	
}
