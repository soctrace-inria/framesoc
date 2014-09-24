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
package fr.inria.soctrace.framesoc.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import fr.inria.soctrace.lib.model.Trace;

/**
 * Handler for the copy DB name command.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class CopyToClipboardHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Trace trace = HandlerCommons.getSelectedTrace(event);
		Clipboard clipboard = new Clipboard(Display.getCurrent());
		TextTransfer textTransfer = TextTransfer.getInstance();
		clipboard.setContents(new Object[] { trace.getDbName() }, new Transfer[] { textTransfer });
	    return null;
	}

}
