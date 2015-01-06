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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;

/**
 * Generic tree label provider with colored square before name.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class OwnerDrawerTreeLabelProvider extends OwnerDrawLabelProvider {

	/**
	 * References to the images (cache).
	 */
	private Map<String, Image> images = new HashMap<>();

	private String getText(Object element) {
		return ((ITreeNode) element).getName();
	}

	@Override
	protected void measure(Event event, Object element) {
		// nothing to do
	}

	@Override
	protected void paint(Event event, Object element) {

		String text = getText(element);

		Rectangle bounds = ((TreeItem) event.item).getBounds(event.index);
		Image img = null;
		if (images.containsKey(text)) {
			img = images.get(text);
		} else {
			img = new Image(event.display, bounds.height / 2, bounds.height / 2);
			GC gc = new GC(img);
			Color swtColor = getColor(element);
			if (swtColor != null && !swtColor.isDisposed()) {
				gc.setBackground(swtColor);
			} else {
				gc.setBackground(FramesocColor.BLACK.getSwtColor());
			}
			gc.fillRectangle(0, 0, bounds.height / 2, bounds.height / 2);
			gc.dispose();
			images.put(text, img);
		}

		// center image and text on y
		bounds.height = bounds.height / 2 - img.getBounds().height / 2;
		int imgy = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;
		int texty = bounds.y + 3;
		event.gc.drawText(text, bounds.x + img.getBounds().width + 5, texty, true);
		event.gc.drawImage(img, bounds.x, imgy);

	}

	@Override
	public void dispose() {
		for (Image img : images.values()) {
			img.dispose();
		}
		images = new HashMap<>();
		super.dispose();
	}

	/**
	 * Get the element color. If no color is defined, return null. If null is returned, the
	 * implementation of {@link #paint(Event, Object)} will choose a default color (e.g., black).
	 * 
	 * @param element
	 *            the element
	 * @return the color, or null if no color is defined.
	 */
	protected abstract Color getColor(Object element);

}
