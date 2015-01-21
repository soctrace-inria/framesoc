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
package fr.inria.soctrace.framesoc.ui.histogram.handler;

import fr.inria.soctrace.framesoc.ui.handlers.ShowTraceHandler;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class ShowEventDensityHandler extends ShowTraceHandler {

	@Override
	public String getViewId() {
		return FramesocViews.HISTOGRAM_VIEW_ID;
	}

}
