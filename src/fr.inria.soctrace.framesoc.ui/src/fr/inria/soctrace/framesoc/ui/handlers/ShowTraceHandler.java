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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.perspective.OpenFramesocPartStatus;
import fr.inria.soctrace.framesoc.ui.utils.TraceSelection;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Base class for handlers showing some representation
 * of the trace (e.g. histogram, pie-chart).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class ShowTraceHandler extends AbstractHandler {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(ShowTraceHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		// get a view already showing this trace or an empty view
		Trace trace = HandlerCommons.getSelectedTrace(event);
		OpenFramesocPartStatus status = FramesocPartManager.getInstance().getPartInstance(getViewId(), trace);
		if (status.part == null) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error", status.message);
			return null;
		}			
		
		// if the trace is already shown we are done
		if (status.part.traceAlreadyShown()) {
			cleanFocus(status.part);
			return null;
		}

		// load the trace otherwise
		DeltaManager dm = new DeltaManager();
		dm.start();
		status.part.showTrace(TraceSelection.getCurrentSelectedTrace(), null);
		logger.debug(dm.endMessage("Load view: " + getViewId()));
		cleanFocus(status.part);
		
		logger.debug("Update titles after opening a new view");
		FramesocPartManager.getInstance().updateTitlesHighlight(TraceSelection.getCurrentSelectedTrace());
			
		return null;
	}
	
	public abstract String getViewId();
	
	/**
	 * Utility method to fix a focus issue.
	 * 
	 * We have to activate another view (we choose the trace details)
	 * and then the view of interest in order to have the trace
	 * selection working in the trace browser.
	 * If we don't do so, the selection in the Trace browser does not work 
	 * anymore until the trace browser doesn't lose the focus.
	 * 
	 * This bad behavior occurs for all views showing graphics (gantt, pie, histogram)
	 * when a view containing the trace is already present.
	 * It does not occur with the event table.
	 * 
	 * This is maybe an Eclipse bug.
	 * 
	 * @param view
	 */
	private void cleanFocus(final FramesocPart view) {
		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				IViewReference[] refs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
				for (IViewReference ref: refs) {
					String id = ref.getId().split(":")[0];
					if (id.equals(FramesocViews.TRACE_DETAILS_VIEW_ID)) {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(ref.getPart(true));
						break;
					}
				}
				view.activateView();				
			}
		});
	}

}
