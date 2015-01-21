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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import fr.inria.soctrace.framesoc.ui.model.ITreeNode;

/**
 * Generic tree label provider
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class TreeLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {		
		return ((ITreeNode)element).getName();
	}
	
	@Override
	public Image getImage(Object element) {		
		return ((ITreeNode)element).getImage();
	}

}
