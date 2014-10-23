/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;


/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AggregatedPieChartLoader extends PieChartLoader {

	/**
	 * Threshold for aggregating percentage values (between 0 and 1).
	 */
	protected static final Double AGGREGATION_THRESHOLD = 0.01;

	/**
	 * Label for aggregated slices.
	 */
	protected static final String AGGREGATED_LABEL = "Aggregated slices";
	
	/**
	 * Color for aggregated slices.
	 */
	protected static final FramesocColor AGGREGATED_COLOR = FramesocColor.BLACK;

	@Override
	public boolean doAggregation() {
		return true;
	}

	@Override
	public double getAggregationThreshold() {
		return AGGREGATION_THRESHOLD;
	}

	@Override
	public String getAggregatedLabel() {
		return AGGREGATED_LABEL;
	}
}