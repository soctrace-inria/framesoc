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
