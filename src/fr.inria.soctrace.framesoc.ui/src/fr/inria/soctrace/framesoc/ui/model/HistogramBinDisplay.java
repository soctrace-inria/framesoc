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
package fr.inria.soctrace.framesoc.ui.model;

import fr.inria.soctrace.lib.model.Trace;

/**
 * Utility class to pass bin display information.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
@Deprecated
public class HistogramBinDisplay {
	
	/**
	 * Currently shown trace in the histogram view.
	 */
	private Trace currentShown;

	/**
	 * Selected bin central timestamp in the Histogram.
	 */
	private long timestamp;

	/**
	 * Constructor 
	 * @param currentShown trace shown
	 * @param timestamp central timestamp in the selected histogram bin
	 */
	public HistogramBinDisplay(Trace currentShown, long timestamp) {
		this.currentShown = currentShown;
		this.timestamp = timestamp;
	}

	/**
	 * @return the currentShown
	 */
	public Trace getCurrentShown() {
		return currentShown;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

}
