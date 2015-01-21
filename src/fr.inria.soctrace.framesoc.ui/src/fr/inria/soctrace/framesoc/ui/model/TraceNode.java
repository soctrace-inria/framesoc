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
package fr.inria.soctrace.framesoc.ui.model;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import fr.inria.soctrace.lib.model.Trace;

/**
 * Tree node for Trace elements (leaves).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceNode implements ITreeNode {

	private String name;
	private FolderNode parent = null;
	private Trace trace = null;
	
	/**
	 * Constructor.
	 * 
	 * @param name trace alias
	 * @param trace trace object
	 */
	public TraceNode(String name, Trace trace) {
		this.name = name;
		this.trace = trace;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
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
		this.parent = (FolderNode) parent;		
	}

	/**
	 * @return the trace object
	 */
	public Trace getTrace() {
		return trace;
	}

	/**
	 * Set the node label.
	 * @param name name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "TraceNode [name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((trace == null) ? 0 : trace.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TraceNode))
			return false;
		TraceNode other = (TraceNode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (trace == null) {
			if (other.trace != null)
				return false;
		} else if (!trace.equals(other.trace))
			return false;
		return true;
	}

}
