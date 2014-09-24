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
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

/**
 * Handler for edit alias command.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
@Deprecated
public class EditAliasHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		Trace trace = HandlerCommons.getSelectedTrace(event);
		InputDialog dialog = new InputDialog(window.getShell(), "Trace Alias", "New trace Alias?", trace.getAlias(), null);
		if (dialog.open() == SWT.CANCEL)
			return null;
		trace.setAlias(dialog.getValue());
		SystemDBObject sysDB = null;
		try {
			sysDB = new SystemDBObject(Configuration.getInstance().get(SoCTraceProperty.soctrace_db_name), DBMode.DB_OPEN);
			sysDB.update(trace);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			DBObject.finalClose(sysDB);
		}
		FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED, true);
		return null;
	}

}
