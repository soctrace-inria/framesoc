package fr.inria.soctrace.framesoc.ui.piechart.providers;

import java.text.NumberFormat;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.piechart.model.IPieChartLoader;
import fr.inria.soctrace.framesoc.ui.piechart.view.StatisticsPieChartView;
import fr.inria.soctrace.framesoc.ui.providers.TableRowLabelProvider;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.TimestampFormat;

public class ValueLabelProvider extends TableRowLabelProvider {

	private StatisticsPieChartView view;

	public ValueLabelProvider(ITableColumn col, StatisticsPieChartView view) {
		super(col);
		this.view = view;
	}

	@Override
	public String getText(Object element) {
		IPieChartLoader loader = view.getCurrentLoader();
		if (loader == null) {
			return super.getText(element);
		} else {
			Double val = Double.valueOf(super.getText(element));
			NumberFormat format = getActualFormat(loader.getFormat(), view.getTimeUnit());
			return format.format(val);
		}
	}
	
	public static NumberFormat getActualFormat(NumberFormat format, TimeUnit unit) {
		if (format instanceof TimestampFormat) {
			TimestampFormat tf = (TimestampFormat) format;
			tf.setTimeUnit(unit);
			return tf;
		} else {
			return format;
		}
	}

}
