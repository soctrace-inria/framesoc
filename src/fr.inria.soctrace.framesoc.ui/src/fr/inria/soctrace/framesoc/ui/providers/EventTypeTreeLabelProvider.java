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
package fr.inria.soctrace.framesoc.ui.providers;

import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.model.EventTypeNode;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;

/**
 * Event type tree label provider
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class EventTypeTreeLabelProvider extends SquareIconLabelProvider {

	@Override
	public String getText(Object element) {
		return ((ITreeNode) element).getName();
	}
	
	@Override
	public Color getColor(Object element) {
		if (element instanceof EventTypeNode) {
			EventTypeNode node = (EventTypeNode) element;
			return FramesocColorManager.getInstance().getEventTypeColor(node.getName())
					.getSwtColor();
		}
		return null;
	}

}
