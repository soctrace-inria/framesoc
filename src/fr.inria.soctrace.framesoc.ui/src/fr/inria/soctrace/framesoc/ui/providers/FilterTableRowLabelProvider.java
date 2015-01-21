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

import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.ResourceManager;

import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.model.ITableColumn;

/**
 * Label provider for filter table row.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FilterTableRowLabelProvider extends TableRowLabelProvider {
	
	public FilterTableRowLabelProvider(ITableColumn col) {
		super(col);
	}

	@Override
	public String getToolTipText(Object element) {
		return "<search regex>";
	}
	
	@Override
	public Image getImage(Object element) {
		return ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/search.png");
	}
	
}
