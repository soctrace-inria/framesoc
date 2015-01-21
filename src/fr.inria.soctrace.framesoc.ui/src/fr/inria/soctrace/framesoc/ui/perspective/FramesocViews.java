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
package fr.inria.soctrace.framesoc.ui.perspective;

import fr.inria.soctrace.framesoc.ui.views.DebugView;

/**
 * Framesoc Views predefined ID.
 * 
 * <p>
 * Other views (views having different IDs from the ones here) 
 * may be managed by Framesoc if:
 * <ul>
 * <li> the plugin providing them extends the 'fr.inria.soctrace.framesoc.ui.perspective.part' extension point
 * <li> the view class inherits from {@link FramesocPart}
 * </ul>
 */
public class FramesocViews {
	
	// Framesoc Debug View
	public static final String DEBUG_VIEW_ID = DebugView.ID;
	
    // Framesoc Static Views
    public static final String TRACE_TREE_VIEW_ID = "fr.inria.soctrace.framesoc.ui.TRACE_EXPLORER"; //$NON-NLS-1$
    public static final String TRACE_DETAILS_VIEW_ID = "fr.inria.soctrace.framesoc.ui.TRACE_METADATA"; //$NON-NLS-1$
        
    /*
     * Views in external plugins.
     * 
     * For these views the ID is statically fixed here,
     * in order to know it during the perspective creation.
     * The plugin providing these views MUST respect 
     * the ID decided here.
     */
    public static final String EVENT_TABLE_VIEW_ID = "fr.inria.soctrace.framesoc.ui.TABLE"; //$NON-NLS-1$
    public static final String HISTOGRAM_VIEW_ID = "fr.inria.soctrace.framesoc.ui.HISTOGRAM"; //$NON-NLS-1$
    public static final String STATISTICS_PIE_CHART_VIEW_ID = "fr.inria.soctrace.framesoc.ui.PIE"; //$NON-NLS-1$
    public static final String GANTT_CHART_VIEW_ID = "fr.inria.soctrace.framesoc.ui.GANTT"; //$NON-NLS-1$
   
}

