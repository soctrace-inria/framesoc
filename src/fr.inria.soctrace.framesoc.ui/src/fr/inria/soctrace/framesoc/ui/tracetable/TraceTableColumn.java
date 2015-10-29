/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Youenn Corre - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.tracetable;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;

/**
 * Class describing a column of the trace filter table
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class TraceTableColumn implements ITableColumn {

	private String shortName;
	private String name;
	private int width;

	public TraceTableColumn(String name, String shortName, int width) {
		this.name = name;
		this.shortName = shortName;
		this.width = width;
	}

	public TraceTableColumn(TraceTableColumnEnum traceColEnum) {
		this.name = traceColEnum.getHeader();
		this.shortName = traceColEnum.getShortName();
		this.width = traceColEnum.getWidth();
	}

	@Override
	public String getHeader() {
		return name;
	}

	@Override
	public int getWidth() {
		return width;
	}

	public String getShortName() {
		return shortName;
	}
}
