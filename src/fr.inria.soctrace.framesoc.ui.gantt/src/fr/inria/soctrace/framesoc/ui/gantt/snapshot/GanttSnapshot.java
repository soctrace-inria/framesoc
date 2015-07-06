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
package fr.inria.soctrace.framesoc.ui.gantt.snapshot;

import fr.inria.soctrace.framesoc.ui.gantt.view.GanttView;
import fr.inria.soctrace.framesoc.ui.utils.Snapshot;


public class GanttSnapshot extends Snapshot {
	private static final String SUFFIX_SNAPSHOT = "_gantt.png";
	private GanttView ganttView;
	private boolean fullHeight = false;
	private boolean includeHeader = true;

	public GanttSnapshot(String directory, GanttView ganttView) {
		super(directory);
		this.ganttView = ganttView;
	}

	@Override
	public void takeSnapShot() {
		// Create and set directory
		snapshotDirectory = createDirectory(ganttView.getCurrentShownTrace()
				.getAlias());

		ganttView.takeSnapshot(width, height, fullHeight, includeHeader,
				snapshotDirectory + "/"
						+ ganttView.getCurrentShownTrace().getAlias()
						+ SUFFIX_SNAPSHOT);
		
		saveTraceConfig(snapshotDirectory);
	}

	@Override
	public String getTraceInfo() {
		StringBuffer output = new StringBuffer();
		output.append("Trace name: ");
		output.append(ganttView.getCurrentShownTrace().getAlias());
		output.append(ganttView.getSnapshotInfo());

		return output.toString();
	}

	
	public boolean isFullHeight() {
		return fullHeight;
	}

	public void setFullHeight(boolean fullHeight) {
		this.fullHeight = fullHeight;
	}

	public boolean isIncludeHeader() {
		return includeHeader;
	}

	public void setIncludeHeader(boolean includeHeader) {
		this.includeHeader = includeHeader;
	}
}
