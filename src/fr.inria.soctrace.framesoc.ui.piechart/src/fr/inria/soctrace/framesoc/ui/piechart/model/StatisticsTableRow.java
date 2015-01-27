/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.piechart.model;

import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
import fr.inria.soctrace.framesoc.ui.model.TableRow;

/**
 * Model element for a row in the statistics table
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class StatisticsTableRow extends TableRow implements ITreeNode {
	
	/**
	 * Parent node
	 */
	protected ITreeNode parent;
	
	/**
	 * Color for the name cell image
	 */
	protected Color color;
	
	/**
	 * Constructor used to create a table row related to a given event type.
	 * @param name event type name
	 * @param occurrences occurrences for that event type
	 * @param percentage percentage for that event type (contains % at the end)
	 * @param color color for the name cell
	 */
	public StatisticsTableRow(String name, String occurrences, String percentage, Color color) {
		this.fields.put(StatisticsTableColumn.NAME, name);
		this.fields.put(StatisticsTableColumn.VALUE, occurrences);
		this.fields.put(StatisticsTableColumn.PERCENTAGE, percentage);
		this.color = color;
	}
	
	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public List<ITreeNode> getChildren() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public ITreeNode getParent() {
		return parent;
	}

	@Override
	public void setParent(ITreeNode parent) {
		this.parent = parent;
	}
}
