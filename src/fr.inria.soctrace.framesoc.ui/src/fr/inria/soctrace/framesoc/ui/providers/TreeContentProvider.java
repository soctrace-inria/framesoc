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
package fr.inria.soctrace.framesoc.ui.providers;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import fr.inria.soctrace.framesoc.ui.model.ITreeNode;

/**
 * Generic tree content provider.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TreeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object parentElement) {
		return ((ITreeNode)parentElement).getChildren().toArray();
	}
 
	@Override
	public Object getParent(Object element) {
		return ((ITreeNode)element).getParent();
	}
 
	@Override
	public boolean hasChildren(Object element) {
		return ((ITreeNode)element).hasChildren();
	}
 
	@Override
	public Object[] getElements(Object inputElement) {
		return (Object[])inputElement;
	}
 
	@Override
	public void dispose() {	
	}
 
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
