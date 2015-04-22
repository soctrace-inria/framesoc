package fr.inria.soctrace.framesoc.ui.treefilter;

import org.eclipse.jface.viewers.IBaseLabelProvider;

import fr.inria.soctrace.framesoc.ui.providers.EventTypeTreeLabelProvider;

public class TypeFilterData implements FilterDimensionData {

	private IBaseLabelProvider labelProvider = new EventTypeTreeLabelProvider();

	@Override
	public FilterDimension getFilterDimension() {
		return FilterDimension.TYPE;
	}

	@Override
	public String getName() {
		return "Event Type";
	}

	@Override
	public String getActionToolTipMessage() {
		return "Show Event Type Filter";
	}
	
	@Override
	public String getDialogMessage() {
		return "Check the Event Types to consider";
	}

	@Override
	public String getIconName() {
		return "type_filter.png";
	}

	@Override
	public String getSetIconName() {
		return "type_filter_set.png";
	}

	@Override
	public String getAppliedIconName() {
		return "type_filter.png";
	}

	@Override
	public IBaseLabelProvider getLabelProvider() {
		return labelProvider;
	}

}
