package fr.inria.soctrace.framesoc.ui.treefilter;

import org.eclipse.jface.viewers.IBaseLabelProvider;

/**
 * Data corresponding to a configuration dimension for a filter.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface FilterDimensionData {	
	
	FilterDimension getFilterDimension();
	
	String getName();
	
	String getActionToolTipMessage();
	
	String getDialogMessage();
	
	String getIconName();
	
	String getSetIconName();
	
	String getAppliedIconName();
	
	IBaseLabelProvider getLabelProvider();
	
}
