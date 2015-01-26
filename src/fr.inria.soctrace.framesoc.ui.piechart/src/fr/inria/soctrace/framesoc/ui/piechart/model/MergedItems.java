/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class MergedItems {

	// merged item label -> merged item
	private Map<String, MergedItem> mergedItems = new HashMap<>();
	// base item label -> merged item
	private Map<String, MergedItem> label2item = new HashMap<>();

	public boolean isEmpty() {
		return mergedItems.isEmpty();
	}

	public List<MergedItem> getMergedItems() {
		return new ArrayList<>(mergedItems.values());
	}

	public void addMergedItem(MergedItem item) {
		// remove the base items of this new merged item from any other old merged item
		for (String baseItem : item.getBaseItems()) {
			if (label2item.containsKey(baseItem)) {
				if (label2item.containsKey(baseItem)) {
					label2item.get(baseItem).removeBaseItem(baseItem);
				}
			}
		}
		// remove a merged item with the same name (if any)
		removeMergedItem(item.getLabel());
		// add the merged item
		mergedItems.put(item.getLabel(), item);
		for (String baseItem : item.getBaseItems()) {
			label2item.put(baseItem, item);
		}
	}

	public void removeMergedItems(List<String> items) {
		for (String item : items) {
			removeMergedItem(item);
		}
	}

	public void removeMergedItem(String item) {
		if (!mergedItems.containsKey(item)) {
			return;
		}
		List<String> baseItems = mergedItems.get(item).getBaseItems();
		for (String baseItem : baseItems) {
			label2item.remove(baseItem);
		}
		mergedItems.remove(item);
	}

	public void removeAllMergedItems() {
		mergedItems = new HashMap<>();
		label2item = new HashMap<>();
	}

	public boolean isPartOfMergedItem(String baseItem) {
		return label2item.containsKey(baseItem);
	}

	public boolean isMergedItem(String label) {
		return mergedItems.containsKey(label);
	}
}
