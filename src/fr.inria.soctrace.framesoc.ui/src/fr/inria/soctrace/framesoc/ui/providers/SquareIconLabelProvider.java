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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;

/**
 * Generic label provider for tree and table viewers, drawing colored squares before names.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class SquareIconLabelProvider extends OwnerDrawLabelProvider implements
		ILabelProvider {

	/**
	 * References to the images (cache).
	 */
	private Map<String, Image> images = new HashMap<>();

	/**
	 * Get the item bounds.
	 * 
	 * @param event paint event
	 * @return the bounds, or null if the item is not a TreeItem or a TableItem
	 */
	private Rectangle getBounds(Event event) {
		if (event.item instanceof TreeItem) {
			return ((TreeItem) event.item).getBounds(event.index);
		}
		if (event.item instanceof TableItem) {
			return ((TableItem) event.item).getBounds(event.index);
		}
		return null;
	}

	@Override
	protected void measure(Event event, Object element) {
		// nothing to do
	}

	@Override
	protected void paint(Event event, Object element) {

		Rectangle bounds = getBounds(event);
		Assert.isNotNull(bounds);

		String text = getText(element);

		Image img = null;
		if (images.containsKey(text)) {
			img = images.get(text);
		} else {
			Color swtColor = getColor(element);
			if (swtColor != null) {
				img = new Image(event.display, bounds.height / 2, bounds.height / 2);
				GC gc = new GC(img);
				if (!swtColor.isDisposed()) {
					/*
					 * We check for disposed because of the following problem: - when I change the
					 * color associated to a type in the color manager, the color manager disposes
					 * the old color associated to that type - the color, however, was cached in the
					 * statistic table row object - such row was not completed here, due to the
					 * exception, thus it was probably requested twice
					 */
					gc.setBackground(swtColor);
				} else {
					gc.setBackground(FramesocColor.BLACK.getSwtColor());
				}
				gc.fillRectangle(0, 0, bounds.height / 2, bounds.height / 2);
				gc.dispose();
				images.put(text, img);
			}
		}

		if (img != null) {
			// center image and text on y
			bounds.height = bounds.height / 2 - img.getBounds().height / 2;
			int imgy = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;
			int texty = bounds.y + 3;
			event.gc.drawText(text, bounds.x + img.getBounds().width + 5, texty, true);
			event.gc.drawImage(img, bounds.x, imgy);
		} else {
			event.gc.drawText(text, bounds.x + 2, bounds.y + 3, true);
		}

	}

	@Override
	public Image getImage(Object element) {
		return null;
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
	 * implementation of {@link #paint(Event, Object)} will not draw the image.
	 * 
	 * @param element
	 *            the element
	 * @return the color, or null if no color is defined.
	 */
	public abstract Color getColor(Object element);

}
