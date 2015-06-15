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
package fr.inria.soctrace.framesoc.ui.histogram.snapshot;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import fr.inria.soctrace.framesoc.ui.histogram.view.HistogramView;
import fr.inria.soctrace.framesoc.ui.utils.Snapshot;

/**
 * Generate a png from the currently displayed event density chart. The width
 * and the height are configurable and the view is redrawn according to these
 * parameters
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class HistogramSnapshot extends Snapshot {

	private static final String SUFFIX_SNAPSHOT = "_histo.png";
	private HistogramView histoView;

	public HistogramSnapshot(String directory, HistogramView histoView) {
		super(directory);
		this.histoView = histoView;
	}

	@Override
	public void takeSnapShot() {
		// Create and set directory
		snapshotDirectory = createDirectory(histoView.getCurrentShownTrace()
				.getAlias());

		// Set output file name
		File outputFile = new File(snapshotDirectory + "/"
				+ histoView.getCurrentShownTrace().getAlias() + SUFFIX_SNAPSHOT);
		try {
			ImageIO.write(histoView.getChartFrame().getChart()
					.createBufferedImage(width, height), "png", outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
