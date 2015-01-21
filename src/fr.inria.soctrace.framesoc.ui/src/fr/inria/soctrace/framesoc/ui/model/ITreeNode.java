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

import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * Generic interface for a tree node.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface ITreeNode {
	
	public String getName();
	
	public Image getImage();
	
	public List<ITreeNode> getChildren();
	
	public boolean hasChildren();
	
	public ITreeNode getParent();
	
	public void setParent(ITreeNode parent);
}
