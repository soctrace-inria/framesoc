/*******************************************************************************
 * Copyright (c) 2012-2014 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.piechart.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.framesoc.ui.model.ITreeNode;

/**
 * Model element for a row in the statistics table
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class StatisticsTableFolderRow extends StatisticsTableRow implements ITreeNode {

	/**
	 * Children rows.
	 * 
	 * Avoid error prone lazy initialization, so that the 0 children case is not different than
	 * other cases for user code.
	 */
	private List<ITreeNode> children = new ArrayList<>();

	/**
	 * Constructor used to create a table row related to an aggregated node
	 * 
	 * @param name
	 *            aggregated name
	 * @param occurrences
	 *            occurrences for that aggregated name
	 * @param percentage
	 *            percentage for that aggregated name (contains % at the end)
	 * @param color
	 *            color for the name cell
	 */
	public StatisticsTableFolderRow(String name, String occurrences, String percentage, Color color) {
		super(name, occurrences, percentage, color);
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	@Override
	public List<ITreeNode> getChildren() {
		return children;
	}

	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public void addChild(ITreeNode child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeAll() {
		children.clear();
	}

}
