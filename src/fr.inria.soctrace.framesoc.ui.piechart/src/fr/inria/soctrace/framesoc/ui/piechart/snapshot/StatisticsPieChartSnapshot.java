package fr.inria.soctrace.framesoc.ui.piechart.snapshot;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import fr.inria.soctrace.framesoc.core.FramesocConstants;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableColumn;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRow;
import fr.inria.soctrace.framesoc.ui.piechart.view.StatisticsPieChartView;
import fr.inria.soctrace.framesoc.ui.utils.Snapshot;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

public class StatisticsPieChartSnapshot extends Snapshot {
	private static final String SUFFIX_SNAPSHOT = "_pie";
	private StatisticsPieChartView pieView;
	private int width;
	private int height;

	public StatisticsPieChartSnapshot(String directory, StatisticsPieChartView pieView) {
		super(directory);
		this.pieView = pieView;
	}

	@Override
	public void takeSnapShot() {
		PrintWriter writer = null;

		// Create and set directory
		snapshotDirectory = createDirectory(pieView.getCurrentShownTrace()
				.getAlias());

		// Set image output file name
		File outputFile = new File(snapshotDirectory + "/"
				+ pieView.getCurrentShownTrace().getAlias() + SUFFIX_SNAPSHOT
				+ ".png");
		try {
		ImageIO.write(pieView.getChartFrame().getChart()
					.createBufferedImage(width, height), "png", outputFile);

			// Get value in CSV file
			String CSVValues = getTableInfo();
			String statOperatorName = pieView.getCurrentLoader().getStatName();

			writer = new PrintWriter(snapshotDirectory + "/"
					+ pieView.getCurrentShownTrace().getAlias() + "_"
					+ statOperatorName + "_" + SUFFIX_SNAPSHOT + ".csv",
					System.getProperty("file.encoding"));
			writer.write(CSVValues);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (writer != null) {
				// Close the fd
				writer.flush();
				writer.close();
			}
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

	public String getTableInfo() {
		StringBuilder valueToCSV = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		// CSV header
		for (StatisticsTableColumn column : StatisticsTableColumn.values())
			valueToCSV.append(column.name() + FramesocConstants.CSV_SEPARATOR);
		
		// Delete last CSV_SEPARATOR
		valueToCSV.delete(
				valueToCSV.length() - FramesocConstants.CSV_SEPARATOR.length(),
				valueToCSV.length());
		
		valueToCSV.append(newLine);

		StatisticsTableRow[] data = (StatisticsTableRow[]) pieView
				.getTableTreeViewer().getInput();
		try {
			for (StatisticsTableRow row : data) {
				for (StatisticsTableColumn column : StatisticsTableColumn
						.values()) {
					valueToCSV.append(row.get(column)
							+ FramesocConstants.CSV_SEPARATOR);
				}
				// remove last CSV_SEPARATOR
				valueToCSV.delete(valueToCSV.length()
						- FramesocConstants.CSV_SEPARATOR.length(),
						valueToCSV.length());

				// New line
				valueToCSV.append(newLine);
			}
		} catch (SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return valueToCSV.toString();
	}
}