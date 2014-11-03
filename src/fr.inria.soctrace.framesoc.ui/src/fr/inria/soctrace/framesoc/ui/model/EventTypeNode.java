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
package fr.inria.soctrace.framesoc.ui.model;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import fr.inria.soctrace.lib.model.EventType;

/**
 * Tree node for Event Type elements (leaves).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventTypeNode implements ITreeNode {

	private ITreeNode parent = null;
	private EventType type = null;
	
	/**
	 * Constructor
	 * @param type the type
	 */
	public EventTypeNode(EventType type) {
		this.type = type;
	}

	@Override
	public String getName() {
		return type.getName();
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public List<ITreeNode> getChildren() {
		return Collections.emptyList();
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

	/**
	 * @return the type
	 */
	public EventType getTrace() {
		return type;
	}

	@Override
	public String toString() {
		return "TraceNode [name=" + getName() + "]";
	}

}
