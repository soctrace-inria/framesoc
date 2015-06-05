package fr.inria.soctrace.framesoc.ui.histogram.view;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import fr.inria.soctrace.framesoc.ui.utils.Snapshot;

public class HistogramSnapshot extends Snapshot {

	private static final String SUFFIX_SNAPSHOT = "_histo.png";
	private HistogramView histoView;
	private int width;
	private int height;

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

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
