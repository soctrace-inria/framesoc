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
package fr.inria.soctrace.tools.importer.otf2.reader;

import java.util.List;

import fr.inria.soctrace.framesoc.core.tools.management.ExternalProgramWrapper;

/**
 * Wrapper for otf2-print program.
 * 
 * It looks for the otf2-print executable path in the configuration file
 * ./<eclipse.dir>/configuration/<plugin.name>/otf2-print.path.
 * 
 * If this file is not found, one is created with a default value, pointing to the precompiled
 * executable (./<plugin.name>/exe/otf2-print).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Otf2PrintWrapper extends ExternalProgramWrapper {

	/**
	 * Constructor
	 * 
	 * @param arguments
	 *            program arguments
	 */
	public Otf2PrintWrapper(List<String> arguments) {
		super(new Otf2PrintConfigManager().readPath(), arguments);
	}

}
