/**
 * 
 */
package fr.inria.soctrace.framesoc.core.tools.model;

/**
 * Interface for Framesoc tools input.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IFramesocToolInput {

	/**
	 * Get the input as a command line string. This is meant to be used with external tools (not
	 * plugins). Plugin tools may simply return an empty string.
	 * 
	 * @return the input command line string
	 */
	String getCommand();

}
