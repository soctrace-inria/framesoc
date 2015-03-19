package fr.inria.soctrace.framesoc.ui.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphFilterDialog;
import fr.inria.soctrace.framesoc.ui.model.CategoryNode;
import fr.inria.soctrace.framesoc.ui.model.EventTypeNode;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;

/**
 * Filter dialog for tree hierarchies.
 * 
 * It provides specify methods for event types and event producers hierarchies.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TreeFilterDialog extends TimeGraphFilterDialog {

	public TreeFilterDialog(Shell parent) {
		super(parent);
	}

	/**
	 * Get the event type hierarchy
	 * 
	 * @param types
	 *            collection of event types
	 * @return an array containing the roots of the type hierarchy
	 */
	public static CategoryNode[] getTypeHierarchy(Collection<EventType> types) {
		Map<Integer, CategoryNode> categories = new HashMap<>();
		for (EventType et : types) {
			EventTypeNode etn = new EventTypeNode(et);
			if (!categories.containsKey(et.getCategory())) {
				categories.put(et.getCategory(), new CategoryNode(et.getCategory()));
			}
			categories.get(et.getCategory()).addChild(etn);
		}
		return categories.values().toArray(new CategoryNode[categories.values().size()]);
	}
	
	/**
	 * Get the event producer hierarchy
	 * 
	 * @param producers
	 *            collection of event producers
	 * @return an array containing the roots of the producer hierarchy
	 */
	public static CategoryNode[] getProducerHierarchy(Collection<EventProducer> producers) {
		// TODO
		return null;
	}

	

}
