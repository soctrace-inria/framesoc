/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.linuxtools.tmf.core.util.Pair;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.piechart.model.IPieChartLoader;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableFolderRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRow;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class PieChartLoader implements IPieChartLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(PieChartLoader.class);

	@Override
	public PieDataset getPieDataset(Map<String, Double> values) {

		Assert.isTrue(values != null, "Null map passed");

		// compute actual threshold and create a sorted list
		Double tot = 0.0;
		List<Pair<String, Double>> sortedValues = new ArrayList<>();
		Iterator<Entry<String, Double>> it = values.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Double> entry = it.next();
			tot += entry.getValue();
			sortedValues.add(new Pair<>(entry.getKey(), entry.getValue()));
		}
		Collections.sort(sortedValues, new ValueComparator());
		Double threshold = tot * getAggregationThreshold();
		boolean aggregate = doAggregation();

		// create dataset
		DefaultPieDataset dataset = new DefaultPieDataset();
		Double aggregatedValue = 0.0;
		for (Pair<String, Double> pair : sortedValues) {
			logger.debug(pair.toString());
			if (aggregate && pair.getSecond() < threshold) {
				aggregatedValue += pair.getSecond();
			} else {
				dataset.setValue(pair.getFirst(), pair.getSecond());
			}
		}
		if (aggregate && aggregatedValue != 0) {
			dataset.setValue(getAggregatedLabel(), aggregatedValue);
		}

		return dataset;
	}

	@Override
	public StatisticsTableFolderRow getTableDataset(Map<String, Double> values) {

		Assert.isTrue(values != null, "Null map passed");

		// compute actual threshold
		Double tot = 0.0;
		for (Double value : values.values()) {
			tot += value;
		}
		Double threshold = tot * getAggregationThreshold();

		// create dataset
		StatisticsTableFolderRow root = new StatisticsTableFolderRow("", "", "", null);
		List<StatisticsTableRow> aggregatedRows = new ArrayList<>();

		boolean aggregate = doAggregation();

		Double aggregatedValue = 0.0;
		Iterator<Entry<String, Double>> it = values.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Double> entry = it.next();
			logger.debug(entry.toString());

			StatisticsTableRow row = new StatisticsTableRow(entry.getKey(), String.valueOf(entry
					.getValue()), getPercentLine(entry.getValue(), tot), getColor(entry.getKey())
					.getSwtColor());

			if (aggregate && entry.getValue() < threshold) {
				aggregatedRows.add(row);
				aggregatedValue += entry.getValue();
			} else {
				root.addChild(row);
			}
		}
		if (aggregate && !aggregatedRows.isEmpty()) {
			StatisticsTableFolderRow agg = new StatisticsTableFolderRow(getAggregatedLabel(),
					String.valueOf(aggregatedValue), getPercentLine(aggregatedValue, tot),
					FramesocColor.BLACK.getSwtColor());
			for (StatisticsTableRow r : aggregatedRows) {
				agg.addChild(r);
			}
			root.addChild(agg);
		}

		return root;
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
	public String toString() {
		return "PieChartLoader [" + getStatName() + "]";
	}

}
