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
package fr.inria.soctrace.framesoc.core.tools.management;


/**
 * Interface for Framesoc tool argument managers.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IArgumentsManager {

	/**
	 * Parse the arguments list
	 * @param args arguments list
	 */
    void parseArgs(String args[]);
    
    /**
     * Performs further elaboration on parsed arguments
     */
    void processArgs();
        
    /**
     * Print the parsed arguments
     */
    void printArgs();

    /**
     * Clean the parsed arguments
     */
	void clean();
    
}
