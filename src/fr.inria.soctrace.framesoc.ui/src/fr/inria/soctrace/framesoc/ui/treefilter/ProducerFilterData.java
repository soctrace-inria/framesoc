package fr.inria.soctrace.framesoc.ui.treefilter;

import org.eclipse.jface.viewers.IBaseLabelProvider;

import fr.inria.soctrace.framesoc.ui.providers.EventProducerTreeLabelProvider;

public class ProducerFilterData implements FilterDimensionData {
	
	private IBaseLabelProvider labelProvider = new EventProducerTreeLabelProvider();

	@Override
	public FilterDimension getFilterDimension() {
		return FilterDimension.PRODUCERS;
	}

	@Override
	public String getName() {
		return "Event Producer";
	}

	@Override
	public String getActionToolTipMessage() {
		return "Show Event Producer Filter";
	}

	@Override
	public String getDialogMessage() {
		return "Check the Event Producers to consider";
	}
	
	@Override
	public String getIconName() {
		return "producer_filter.png";
	}

	@Override
	public String getSetIconName() {
		return "producer_filter_set.png";
	}

	@Override
	public String getAppliedIconName() {
		return "producer_filter.png";
	}

	@Override
	public IBaseLabelProvider getLabelProvider() {
		return labelProvider;
	}

}
