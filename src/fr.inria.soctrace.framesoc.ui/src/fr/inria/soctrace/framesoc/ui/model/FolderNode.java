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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Tree node for folders.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FolderNode implements ITreeNode {

	private String name;
	private ITreeNode parent = null;
	private List<ITreeNode> children = new ArrayList<>();
	
	/**
	 * Constructor
	 * @param name folder label
	 */
	public FolderNode(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	}

	@Override
	public List<ITreeNode> getChildren() {
		return children;
	}

	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
	public ITreeNode getParent() {
		return parent;
	}
	
	/**
	 * Add a child node to the folder.
	 * @param child a tree node
	 */
	public void addChild(ITreeNode child) {
		children.add(child);
		child.setParent(this);
	}

	/**
	 * Remove all the children from this folder.
	 */
	public void removeAll() {
		children.clear();
	}
	
	@Override
	public void setParent(ITreeNode parent) {
		this.parent = (FolderNode)parent;		
	}

	@Override
	public String toString() {
		return "FolderNode [name=" + name + ", parent=" + parent + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	/**
	 * children not considered to avoid recursion.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FolderNode))
			return false;
		FolderNode other = (FolderNode) obj;
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
		return true;
	}

}
