/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class StateDurationPieChartLoader extends DurationPieChartLoader {

	@Override
	public String getStatName() {
		return "State duration";
	}

	@Override
	protected int getDurationCategory() {
		return EventCategory.STATE;
	}

}
