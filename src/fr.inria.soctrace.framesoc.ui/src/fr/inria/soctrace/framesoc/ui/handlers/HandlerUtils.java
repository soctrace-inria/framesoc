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
package fr.inria.soctrace.framesoc.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Utility classes to deal with handlers from external modules.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class HandlerUtils {

	/**
	 * Utility to launch a command given the Workbench site and the
	 * command identifier.
	 * 
	 * @param site Workbench site
	 * @param commandID command identifier
	 */
	public static void launchCommand(IWorkbenchPartSite site, String commandID) {
		IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);
		try {
			handlerService.executeCommand(commandID, null);
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
			e.printStackTrace();
		}
	}
}
