package fr.inria.soctrace.framesoc.ui.treefilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Shell;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphFilterDialog;
import fr.inria.soctrace.framesoc.ui.model.CategoryNode;
import fr.inria.soctrace.framesoc.ui.model.EventProducerNode;
import fr.inria.soctrace.framesoc.ui.model.EventTypeNode;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
import fr.inria.soctrace.framesoc.ui.utils.AlphanumComparator;
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
	
	@Override
	public void setContentProvider(ITreeContentProvider contentProvider) {
		super.setContentProvider(contentProvider);
	}
	
	@Override
	public void setColumnNames(String[] columnNames) {
		super.setColumnNames(columnNames);
	}
	
	@Override
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		super.setLabelProvider(labelProvider);
	}
	
	@Override
    public void setInput(Object input) {
		super.setInput(input);
	}
	
	@Override
	public void setExpandedElements(Object[] elements) {
		super.setExpandedElements(elements);
	}
	
	@Override
    public void create() {
		super.create();
	}
	
	@Override
	public int open() {
		return super.open();
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
		List<EventType> eTypes = (List<EventType>) types;
		
		// Sort the types alphabetically
		Collections.sort(eTypes, new Comparator<EventType>() {
			@Override
			public int compare(EventType o1, EventType o2) {
				return AlphanumComparator.compare(o1.getName(), o2.getName());
			}
		});
		
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
		Map<Long, EventProducerNode> prodMap = new HashMap<>();
		for (EventProducer ep : producers) {
			prodMap.put(ep.getId(), new EventProducerNode(ep));
		}
		List<EventProducerNode> roots = new ArrayList<>();
		
		List<EventProducer> lProducers = (List<EventProducer>) producers;
		
		// Sort the producers alphabetically
		Collections.sort(lProducers, new Comparator<EventProducer>() {
			@Override
			public int compare(EventProducer o1, EventProducer o2) {
				return AlphanumComparator.compare(o1.getName(), o2.getName());
			}
		});
		
		for (EventProducer ep : lProducers) {
			EventProducerNode node = prodMap.get(ep.getId());
			Long parentId = ep.getParentId();
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
	
	/**
	 * Explores the list of top-level inputs and returns all the inputs
	 * 
	 * @param inputs
	 *            The top-level inputs
	 * @return All the inputs
	 */
	public static List<Object> listAllInputs(List<? extends ITreeNode> inputs) {
		ArrayList<Object> items = new ArrayList<>();
		for (ITreeNode entry : inputs) {
			items.add(entry);
			if (entry.hasChildren()) {
				items.addAll(listAllInputs(entry.getChildren()));
			}
		}
		return items;
	}
	  
	public static void printHierarchy(Collection<? extends ITreeNode> roots, String tab) {
		for (ITreeNode node : roots) {
			System.out.println(tab + node.getName());
			if (node.hasChildren()) {
				printHierarchy(node.getChildren(), tab + " ");
			}
		}
	}

}
