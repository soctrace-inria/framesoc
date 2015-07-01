/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.core;
import org.eclipse.ui.IStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Eclipse startups class for Framesoc core plugin.
 * It does nothing at the moment.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocStartup implements IStartup {

	/**
	 * Logger
	 */
	private static Logger logger = LoggerFactory
			.getLogger(FramesocStartup.class);

	public static boolean Started = false;

	@Override
	public void earlyStartup() {

		logger.debug("Framesoc Startup");
		Started = true;

		// nothing at the moment
	}

}
