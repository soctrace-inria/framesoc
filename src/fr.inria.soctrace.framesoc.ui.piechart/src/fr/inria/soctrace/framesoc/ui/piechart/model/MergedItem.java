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
	private List<String> aggregatedItems;
	
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
	
	public List<String> getAggregatedItems() {
		return aggregatedItems;
	}
	
	public void setAggregatedItems(List<String> aggregatedItems) {
		this.aggregatedItems = aggregatedItems;
	}
}
