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
package fr.inria.soctrace.framesoc.ui.piechart.model;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jfree.data.general.PieDataset;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Interface for pie chart statistics loaders.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IPieChartLoader {

	/**
	 * Return the name of the computed statistics (e.g. Event Types, Event Producers)
	 * 
	 * @return the statistics name
	 */
	String getStatName();

	/**
	 * Return true if aggregation is performed.
	 * 
	 * @return true if aggregation is performed, false otherwise
	 */
	boolean doAggregation();

	/**
	 * Get the aggregation threshold percentage (i.e., 0.1 is 10%). It is considered only if
	 * {@link #doAggregation()} returns true.
	 * 
	 * @return the aggregation threshold percentages
	 */
	double getAggregationThreshold();

	/**
	 * Get the label for the aggregated items.
	 * 
	 * @return the label for the aggregated items
	 */
	String getAggregatedLabel();

	/**
	 * Load the statistics for the given trace and time interval into the passed map.
	 * 
	 * @param trace
	 *            trace to work with
	 * @param interval
	 *            time interval to load
	 * @param map
	 *            loader map to fill
	 * @param monitor
	 *            progress monitor
	 * @throws SoCTraceException
	 */
	void load(Trace trace, TimeInterval interval, PieChartLoaderMap map, IProgressMonitor monitor);

	/**
	 * Get the color corresponding to the given item.
	 * 
	 * @param name
	 *            item name
	 * @return the corresponding color
	 */
	FramesocColor getColor(String name);

	/**
	 * Get the pie dataset, performing aggregation if necessary.
	 * 
	 * @param map
	 *            loaded map
	 * @return the pie dataset
	 * @throws SoCTraceException
	 */
	PieDataset getPieDataset(Map<String, Double> values);

	/**
	 * Get the statistics table rows corresponding to the loaded values
	 * 
	 * @param map
	 *            loaded map
	 * @return the roots row
	 * @throws SoCTraceException
	 */
	StatisticsTableRow[] getTableDataset(Map<String, Double> values);

}
