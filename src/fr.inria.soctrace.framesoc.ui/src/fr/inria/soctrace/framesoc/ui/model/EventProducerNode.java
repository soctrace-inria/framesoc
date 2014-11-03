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

import fr.inria.soctrace.lib.model.EventProducer;

/**
 * Tree node for event producers.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventProducerNode implements ITreeNode {

	private EventProducer producer = null;
	private ITreeNode parent = null;
	private List<ITreeNode> children = new ArrayList<>();
	
	/**
	 * Constructor
	 * @param name folder label
	 */
	public EventProducerNode(EventProducer ep) {
		this.producer = ep;
	}

	@Override
	public String getName() {
		return producer.getWholeName();
	}

	@Override
	public Image getImage() {
		return null;
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
		this.parent = parent;		
	}

	@Override
	public String toString() {
		return "EventProducerNode [name=" + getName() + ", parent=" + parent.getName() + "]";
	}

	public EventProducer getEventProducer() {
		return producer;
	}
}
