/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.model;

import java.util.List;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class MergedItem {
	
	private String label;
	private FramesocColor color;
	private List<String> baseItems;
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public FramesocColor getColor() {
		return color;
	}
	
	public void setColor(FramesocColor color) {
		this.color = color;
	}
	
	public List<String> getBaseItems() {
		return baseItems;
	}
	
	public void setBaseItems(List<String> baseItems) {
		this.baseItems = baseItems;
	}
	
	public void removeBaseItem(String baseItem) {
		this.baseItems.remove(baseItem);
	}
	
	public void addBaseItem(String baseItem) {
		this.baseItems.add(baseItem);
	}
	
}
