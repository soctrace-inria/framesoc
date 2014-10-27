/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;

/**
 * Pie Chart loader for link duration.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class LinkDurationPieChartLoader extends DurationPieChartLoader {

	@Override
	public String getStatName() {
		return "Link duration";
	}

	@Override
	protected int getDurationCategory() {
		return EventCategory.LINK;
	}

}
