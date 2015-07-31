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

import java.text.NumberFormat;
import javafx.scene.chart.PieChart;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import javafx.collections.ObservableList;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Interface for pie chart statistics loaders.
 * 
 * The data loaded are conceptually a list of (name, value) pairs. Names are required to be unique.
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

	/*
	 * Loader properties
	 */

	/**
	 * Return true if aggregation is performed.
	 * 
	 * @return true if aggregation is performed, false otherwise
	 */
	boolean isAggregationSupported();

	/**
	 * Get the aggregation threshold percentage (i.e., 0.1 is 10%). It is considered only if
	 * {@link #isAggregationSupported()} returns true.
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
	 * Get the format to be used for the value.
	 * 
	 * @return
	 */
	NumberFormat getFormat();

	/*
	 * Loading data
	 */

	/**
	 * Specify the list of event producer IDs that must be used in statistics computation.
	 * By default, all producers are used if this method is not called.
	 * 
	 * @param producers list of Event Producer IDs
	 */
	void setEventProducerFilter(List<Integer> producers);

	/**
	 * Specify the list of event type IDs that must be used in statistics computation.
	 * By default, all types are used if this method is not called.
	 * 
	 * @param types list of Event Type IDs
	 */
	void setEventTypeFilter(List<Integer> types);

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

	/*
	 * Getting data-sets
	 */

	/**
	 * Get the pie dataset, performing aggregation if necessary.
	 * 
	 * @param map
	 *            loaded map
	 * @param excluded
	 *            list of excluded items
	 * @param merged
	 *            list of merged items
	 * @return the pie dataset
	 */
	ObservableList<PieChart.Data> getPieDataset(Map<String, Double> values, List<String> excluded,
			List<MergedItem> merged);

	/**
	 * Get the statistics table rows corresponding to the loaded values
	 * 
	 * @param map
	 *            loaded map
	 * @param excluded
	 *            list of excluded items
	 * @param merged
	 *            list of merged items
	 * @return the roots row
	 */
	List<StatisticsTableRow> getTableDataset(Map<String, Double> values, List<String> excluded,
			List<MergedItem> merged);

	/*
	 * Getting GUI information
	 */

	/**
	 * Update the information about used labels.
	 * 
	 * @param values
	 *            loaded map
	 * @param merged
	 *            list of merged items
	 */
	void updateLabels(Map<String, Double> values, List<MergedItem> merged);

	/**
	 * Get the color corresponding to the given item.
	 * 
	 * Before calling this method, be sure to call {@link #updateLabels(Map, List)}.
	 * 
	 * @param name
	 *            item name
	 * @return the corresponding color
	 */
	FramesocColor getColor(String name);

	/**
	 * Check whether the passed label can be used.
	 * 
	 * Labels must be unique, so a label can be used if: (1) there is no other aggregate with this
	 * label, (2) there is no base item with this label, (3) it is not equal to the aggregated
	 * label.
	 * 
	 * Before calling this method, be sure to call {@link #updateLabels(Map, List)}.
	 * 
	 * @param label
	 *            label to check
	 * @return true if the label can be used
	 */
	public boolean checkLabel(String label);

}
