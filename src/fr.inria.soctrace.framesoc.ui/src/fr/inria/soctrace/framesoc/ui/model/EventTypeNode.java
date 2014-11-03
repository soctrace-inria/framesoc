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
	public EventType getEventType() {
		return type;
	}

	@Override
	public String toString() {
		return "EventTypeNode [name=" + getName() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventTypeNode other = (EventTypeNode) obj;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
