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
package fr.inria.soctrace.framesoc.core.tools.model;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.framesoc.core.tools.management.PluginToolJob;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Interface to be implemented by classes that need to be executed inside
 * a {@link PluginToolJob} (or one of its derived classes).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IPluginToolJobBody {
	
	/**
	 * The body of the Job
	 * @param monitor progress monitor to intercept user actions (i.e., cancel)
	 * @throws SoCTraceException
	 */
	void run(IProgressMonitor monitor) throws SoCTraceException;
	
}
