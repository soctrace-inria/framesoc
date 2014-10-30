package fr.inria.soctrace.tools.importer.otf2.core;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Interface for OTF2 line parsers.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
interface Otf2LineParser {

	/**
	 * Parse the line
	 * 
	 * @param String
	 *            containing the text line
	 * @throws SoCTraceException
	 */
	void parseLine(String type, String fields) throws SoCTraceException;

}
