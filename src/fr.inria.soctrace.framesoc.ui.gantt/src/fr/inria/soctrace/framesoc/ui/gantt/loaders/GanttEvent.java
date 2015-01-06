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
package fr.inria.soctrace.framesoc.ui.gantt.loaders;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Default implementation of Gantt Event
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class GanttEvent extends TimeEvent {

	public GanttEvent(ITimeGraphEntry entry, long time, long duration, int value) {
		super(entry, time, duration, value);
	}

	public GanttEvent(ITimeGraphEntry entry, long time, long duration) {
		super(entry, time, duration);
	}
	
}
