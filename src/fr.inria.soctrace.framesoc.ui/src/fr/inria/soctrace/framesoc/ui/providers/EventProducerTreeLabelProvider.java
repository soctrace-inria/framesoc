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
import fr.inria.soctrace.framesoc.ui.model.EventProducerNode;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;

/**
 * Event producer tree label provider
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class EventProducerTreeLabelProvider extends SquareIconLabelProvider {

	@Override
	protected String getText(Object element) {
		return ((ITreeNode) element).getName();
	}

	@Override
	protected Color getColor(Object element) {
		if (element instanceof EventProducerNode) {
			EventProducerNode node = (EventProducerNode) element;
			return FramesocColorManager.getInstance().getEventProducerColor(node.getName())
					.getSwtColor();
		}
		return null;
	}

}
