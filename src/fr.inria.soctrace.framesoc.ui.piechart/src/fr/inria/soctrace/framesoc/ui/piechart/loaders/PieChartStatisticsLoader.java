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
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableFolderRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRow;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Base abstract class for pie chart statistics loaders
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class PieChartStatisticsLoader {
	
	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(PieChartStatisticsLoader.class);

	/**
	 * Label for aggregated slices.
	 */
	protected static final String AGGREGATED_LABEL = "Aggregated slices";
	
	/**
	 * Threshold for aggregating percentage values (between 0 and 1).
	 */
	protected static final Double AGGREGATION_THRESHOLD = 0.01;
	
	/**
	 * Map between the label and the value, containing raw (non aggregated) data.
	 * This is loaded by the load method.
	 */
	protected Map<String, Double> values;
	
	/**
	 * Return the name of the computed statistics 
	 * (e.g. Event Types, Event Producers)
	 * 
	 * @return the statistics name
	 */
	public abstract String getStatName();
		
	/**
	 * Load a raw dataset for the statistics Pie Chart.
	 * This method must be called before calling 
	 * getPieDataset(), getTableRows() and getColors().
	 * 
	 * @param trace trace to work with
	 * @throws SoCTraceException
	 */
	public abstract void load(Trace trace) throws SoCTraceException;

	/**
	 * Get the mapping between the raw data labels and the colors.
	 *  
	 * @return the color mapping
	 * @throws SoCTraceException 
	 */
	protected abstract Map<String, FramesocColor> getRawColors() throws SoCTraceException;
	
	/**
	 * Get the mapping between the names and the colors,
	 * containing both raw data and aggregated data labels.
	 * 
	 * @return the color mapping
	 * @throws SoCTraceException 
	 */
	public Map<String, FramesocColor> getColors() throws SoCTraceException {
		Map<String, FramesocColor> cmap = getRawColors();
		cmap.put(AGGREGATED_LABEL, FramesocColor.BLACK);
		return cmap;
	}
	
	/**
	 * Get the pie dataset, with aggregation.
	 * 
	 * @return the pie dataset
	 * @throws SoCTraceException
	 */
	public PieDataset getPieDataset() throws SoCTraceException {

		if (values == null || values.isEmpty())
			throw new SoCTraceException("Values not loaded");
		
		// compute actual threshold and create a sorted map
		Double tot = 0.0;
		ValueComparator vc = new ValueComparator(values);
		TreeMap<String, Double> sortedMap = new TreeMap<String, Double>(vc);
		Iterator<Entry<String, Double>> it = values.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Double> entry = it.next();
			tot += entry.getValue();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		Double threshold = tot*AGGREGATION_THRESHOLD;		
		
		// create dataset
		DefaultPieDataset dataset = new DefaultPieDataset();
		Double aggregatedValue = 0.0;		
		it = sortedMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Double> entry = it.next();
			logger.debug(entry.toString());
			if (entry.getValue() < threshold) {
				aggregatedValue += entry.getValue();
			} else {
				dataset.setValue(entry.getKey(), entry.getValue());;				
			}
		}
		if (aggregatedValue!=0)
			dataset.setValue(AGGREGATED_LABEL, aggregatedValue);
		
		return dataset;
	}
	
	/**
	 * Get the statistics table rows corresponding to the loaded values
	 * 
	 * @param colors colors
	 * @throws SoCTraceException 
	 */
	public StatisticsTableFolderRow getTableRows(Map<String, FramesocColor> colors) throws SoCTraceException {

		if (values == null || values.isEmpty())
			throw new SoCTraceException("Values not loaded");

		// compute actual threshold and create a sorted map
		Double tot = 0.0;
		Iterator<Entry<String, Double>> it = values.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Double> entry = it.next();
			tot += entry.getValue();
		}
		Double threshold = tot*AGGREGATION_THRESHOLD;		
		
		// create dataset
		StatisticsTableFolderRow root = new StatisticsTableFolderRow("", "", "", null);
		List<StatisticsTableRow> aggregatedRows = new LinkedList<StatisticsTableRow>();
		
		Double aggregatedValue = 0.0;		
		it = values.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Double> entry = it.next();
			logger.debug(entry.toString());
			
			StatisticsTableRow row = new StatisticsTableRow(entry.getKey(), String.valueOf(entry.getValue()), 
					getPercentLine(entry.getValue(), tot), colors.get(entry.getKey()).getSwtColor());
			
			if (entry.getValue() < threshold) {
				aggregatedRows.add(row);
				aggregatedValue += entry.getValue();
			} else {
				root.addChild(row);				
			}
			
		}
		if (aggregatedValue!=0) {
			StatisticsTableFolderRow agg = new StatisticsTableFolderRow(AGGREGATED_LABEL, 
					String.valueOf(aggregatedValue), getPercentLine(aggregatedValue, tot), 
					colors.get(AGGREGATED_LABEL).getSwtColor());
			for (StatisticsTableRow r: aggregatedRows) {
				agg.addChild(r);
			}
			root.addChild(agg);
		}
		
		return root;
	}
	
	public int getNumberOfValues() {
		if (values == null || values.isEmpty())
			return 0;
		return values.size();
	}
	
	private String getPercentLine(double val, double tot) {
		Double pValue = (val * 100 )/ tot;
		NumberFormat formatter = new DecimalFormat("#0.00");  
		return formatter.format(pValue) + " %";
	}
	
	class ValueComparator implements Comparator<String> {

	    private Map<String, Double> base;
	    
	    public ValueComparator(Map<String, Double> base) {
	        this.base = base;
	    }
    
	    // descending ordering
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
}

