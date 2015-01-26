/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.linuxtools.tmf.core.util.Pair;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.piechart.model.IPieChartLoader;
import fr.inria.soctrace.framesoc.ui.piechart.model.MergedItem;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableFolderRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRow;

/**
 * Base abstract class for Pie Chart loaders.
 * 
 * <pre>
 * It provides the following functionalities:
 * - aggregating items whose percentage is lower than a threshold
 * - merging items in a merged item
 * - excluding items from statistics
 * </pre>
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class PieChartLoader implements IPieChartLoader {

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(PieChartLoader.class);
	
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

	/**
	 * Merged label colors
	 */
	protected Map<String, FramesocColor> mergedLabels = new HashMap<>();

	/**
	 * Get the color for a real entity whose name is passed.
	 * 
	 * By real entity we mean something that is not an aggregate, 
	 * but a leaf entity.
	 * 
	 * @param name entity name
	 * @return the corresponding color
	 */
	protected abstract FramesocColor getBaseColor(String name);

	@Override
	public FramesocColor getColor(String name) {
		if (name.equals(AGGREGATED_LABEL)) {
			return AGGREGATED_COLOR;
		} else if (mergedLabels.containsKey(name)) {
			return mergedLabels.get(name);
		}
		return getBaseColor(name);
	}
	
	@Override
	public PieDataset getPieDataset(Map<String, Double> values, List<String> excluded,
			List<MergedItem> merged) {

		Assert.isTrue(values != null, "Null map passed");

		// refresh merged label colors
		loadMergedLabels(merged);

		// set of excluded rows
		Set<String> excludedSet = new HashSet<>();
		for (String h : excluded) {
			excludedSet.add(h);
		}

		// compute total and actual threshold
		Double tot = getTotal(excludedSet, values);
		Double threshold = tot * getAggregationThreshold();

		// create a list of merged slices
		Set<String> mergedSet = new HashSet<>();
		List<Pair<String, Double>> slices = new ArrayList<>();
		for (MergedItem i : merged) {
			Double val = 0.0;
			for (String s : i.getMergedItems()) {
				// add the label to the merged set, in order to skip the slice after
				mergedSet.add(s);
				val += values.get(s);
			}
			slices.add(new Pair<>(i.getLabel(), val));
		}

		// add the other slices to the list and sort it
		Iterator<Entry<String, Double>> it = values.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Double> entry = it.next();
			if (excludedSet.contains(entry.getKey()) || mergedSet.contains(entry.getKey())) {
				// skip excluded and merged rows
				continue;
			}
			slices.add(new Pair<>(entry.getKey(), entry.getValue()));
		}
		Collections.sort(slices, new ValueComparator());

		// create dataset
		boolean aggregate = isAggregationSupported();
		boolean isThereAnyAggregate = false;
		DefaultPieDataset dataset = new DefaultPieDataset();
		Double aggregatedValue = 0.0;
		for (Pair<String, Double> pair : slices) {
			logger.debug(pair.toString());
			if (aggregate && pair.getSecond() < threshold) {
				aggregatedValue += pair.getSecond();
				isThereAnyAggregate = true;
			} else {
				dataset.setValue(pair.getFirst(), pair.getSecond());
			}
		}
		if (isThereAnyAggregate) {
			dataset.setValue(getAggregatedLabel(), aggregatedValue);
		}

		return dataset;
	}

	@Override
	public StatisticsTableRow[] getTableDataset(Map<String, Double> values, List<String> excluded,
			List<MergedItem> merged) {

		Assert.isTrue(values != null, "Null map passed");

		// refresh merged label colors
		loadMergedLabels(merged);
		
		// set of excluded rows
		Set<String> excludedSet = new HashSet<>();
		for (String h : excluded) {
			excludedSet.add(h);
		}

		// compute total and actual threshold
		Double tot = getTotal(excludedSet, values);
		Double threshold = tot * getAggregationThreshold();

		// create dataset for merged rows
		List<StatisticsTableRow> roots = new ArrayList<>();
		Set<String> mergedSet = new HashSet<>();
		for (MergedItem m : merged) {
			// create merged rows
			List<StatisticsTableRow> rows = new ArrayList<>();
			Double mTot = 0.0;
			for (String a : m.getMergedItems()) {
				if (excluded.contains(a)) {
					continue;
				}
				// add the label to the merged set, in order to skip the row after
				mergedSet.add(a);
				Double val = values.get(a);
				mTot += val;
				StatisticsTableRow ar = new StatisticsTableRow(a, val.toString(), getPercentLine(
						val, tot), getColor(a).getSwtColor());
				rows.add(ar);
			}
			// create folder row
			if (!rows.isEmpty()) {
				StatisticsTableFolderRow folderRow = new StatisticsTableFolderRow(m.getLabel(),
						mTot.toString(), getPercentLine(mTot, tot), m.getColor().getSwtColor());
				for (StatisticsTableRow ar : rows) {
					folderRow.addChild(ar);
				}
				roots.add(folderRow);
			}
		}

		// create dataset for all the other rows
		boolean aggregate = isAggregationSupported();
		List<StatisticsTableRow> aggregatedRows = new ArrayList<>();
		Double aggregatedValue = 0.0;
		Iterator<Entry<String, Double>> it = values.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Double> entry = it.next();
			if (excludedSet.contains(entry.getKey()) || mergedSet.contains(entry.getKey())) {
				// skip excluded and merged rows
				continue;
			}
			logger.debug(entry.toString());
			StatisticsTableRow row = new StatisticsTableRow(entry.getKey(), String.valueOf(entry
					.getValue()), getPercentLine(entry.getValue(), tot), getColor(entry.getKey())
					.getSwtColor());

			if (aggregate && entry.getValue() < threshold) {
				aggregatedRows.add(row);
				aggregatedValue += entry.getValue();
			} else {
				roots.add(row);
			}
		}
		if (!aggregatedRows.isEmpty()) {
			StatisticsTableFolderRow agg = new StatisticsTableFolderRow(getAggregatedLabel(),
					String.valueOf(aggregatedValue), getPercentLine(aggregatedValue, tot),
					FramesocColor.BLACK.getSwtColor());
			for (StatisticsTableRow r : aggregatedRows) {
				agg.addChild(r);
			}
			roots.add(agg);
		}

		return roots.toArray(new StatisticsTableRow[roots.size()]);
	}

	private void loadMergedLabels(List<MergedItem> merged) {
		mergedLabels = new HashMap<>();
		for (MergedItem i : merged) {
			mergedLabels.put(i.getLabel(), i.getColor());
		}
	}
	
	private Double getTotal(Set<String> hiddenSet, Map<String, Double> values) {
		Double tot = 0.0;
		Iterator<Entry<String, Double>> it = values.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Double> entry = it.next();
			if (!hiddenSet.contains(entry.getKey())) {
				tot += entry.getValue();
			}
		}
		return tot;
	}

	private String getPercentLine(double val, double tot) {
		Double pValue = (val * 100) / tot;
		NumberFormat formatter = new DecimalFormat("#0.00");
		return formatter.format(pValue) + " %";
	}

	class ValueComparator implements Comparator<Pair<String, Double>> {
		@Override
		public int compare(Pair<String, Double> p1, Pair<String, Double> p2) {
			// descending order
			return -1 * Double.compare(p1.getSecond(), p2.getSecond());
		}
	}

	@Override
	public boolean isAggregationSupported() {
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

	@Override
	public String toString() {
		return "PieChartLoader [" + getStatName() + "]";
	}

}
