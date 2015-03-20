package fr.inria.soctrace.framesoc.ui.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphFilterDialog;
import fr.inria.soctrace.framesoc.ui.model.CategoryNode;
import fr.inria.soctrace.framesoc.ui.model.EventProducerNode;
import fr.inria.soctrace.framesoc.ui.model.EventTypeNode;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
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
	public static EventProducerNode[] getProducerHierarchy(Collection<EventProducer> producers) {
		Map<Integer, EventProducerNode> prodMap = new HashMap<>();
		for (EventProducer ep : producers) {
			prodMap.put(ep.getId(), new EventProducerNode(ep));
		}
		List<EventProducerNode> roots = new ArrayList<>();
		for (EventProducer ep : producers) {
			EventProducerNode node = prodMap.get(ep.getId());
			int parentId = ep.getParentId();
			if (parentId == EventProducer.NO_PARENT_ID) {
				roots.add(node);
			} else {
				EventProducerNode parentNode = prodMap.get(parentId);
				if (parentNode == null) {
					StringBuilder sb = new StringBuilder();
					sb.append("The passed collection of producers contains a broken hierarchy: ");
					sb.append("parent of ");
					sb.append(ep.getWholeName());
					sb.append(" not found (parent id=");
					sb.append(ep.getParentId());
					sb.append(").");
					throw new IllegalArgumentException(sb.toString());
				}
				parentNode.addChild(node);
			}
		}
		return roots.toArray(new EventProducerNode[roots.size()]);
	}
	
	public static void printHierarchy(Collection<ITreeNode> roots, String tab) {
		for (ITreeNode node : roots) {
			System.out.println(tab + node.getName());
			if (node.hasChildren()) {
				printHierarchy(node.getChildren(), tab + " ");
			}
		}
	}

}
