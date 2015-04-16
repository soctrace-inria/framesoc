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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import fr.inria.soctrace.framesoc.ui.init.Initializer;

/**
 * Handler for the initialize system command.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class InitializeSystem extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		if (Initializer.INSTANCE.initializeSystem(window.getShell(), false)) {
			Initializer.INSTANCE.manageTools(window.getShell());
		}
		
		return null;
	}

}