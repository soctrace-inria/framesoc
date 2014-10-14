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
package fr.inria.soctrace.framesoc.ui.piechart.providers;

import java.util.Map;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRow;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Label provider for StatisticsTableRow objects.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class StatisticsTableRowLabelProvider extends OwnerDrawLabelProvider {
	
	/**
	 * Managed column
	 */
	protected ITableColumn col;
	
	/**
	 * References to the images (cache).
	 * The ownership is to the statistics view.
	 */
	protected Map<String, Image> images;
	
	/**
	 * Constructor
	 * @param col ITableColumn the provider is related to.
	 * @param images 
	 */
	public StatisticsTableRowLabelProvider(ITableColumn col, Map<String, Image> images) {
		this.col = col;
		this.images = images;
	}
	
	@Override
	protected void measure(Event event, Object element) {
		// nothing to do
	}

	@Override
	protected void paint(Event event, Object element) {
		
		String text = "";
		try {
			text = ((ITableRow) element).get(col);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		
		Rectangle bounds = ((TreeItem) event.item).getBounds(event.index);
		Image img = null;
		if (images.containsKey(text)) {
			img = images.get(text);
		} else {
			img = new Image(event.display, bounds.height/2, bounds.height/2);
			GC gc = new GC(img);
			StatisticsTableRow row = (StatisticsTableRow)element;
			Color swtColor = row.getColor();
			/* Problem:
			 * - when I change the color associated to a type in the color manager,
			 *   the color manager disposes the old color associated to that type
			 * - the color, however, was cached in the statistic table row object
			 * - such row was not completed here, due to the exception, thus it was 
			 *   probably requested twice
			 * */
			if (!swtColor.isDisposed()) 
				gc.setBackground(swtColor);
			else {
				System.out.println(row);
			}
		    gc.fillRectangle(0, 0, bounds.height/2, bounds.height/2);
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
	
}
