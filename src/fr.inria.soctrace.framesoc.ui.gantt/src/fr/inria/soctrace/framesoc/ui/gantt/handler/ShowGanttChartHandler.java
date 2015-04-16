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
package fr.inria.soctrace.framesoc.ui.gantt.handler;

import fr.inria.soctrace.framesoc.ui.gantt.view.GanttView;
import fr.inria.soctrace.framesoc.ui.handlers.ShowTraceHandler;

/**
 * Handler for show Gantt chart command.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ShowGanttChartHandler extends ShowTraceHandler {

	private final static String VIEW_NAME = "Gantt Chart";

	@Override
	public String getViewId() {
		return GanttView.ID;
	}

	@Override
	public String getViewName() {
		return VIEW_NAME;
	}

}
