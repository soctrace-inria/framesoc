package fr.inria.soctrace.framesoc.ui.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.utils.Portability;

public abstract class Snapshot {

	private static final Logger logger = LoggerFactory
			.getLogger(Snapshot.class);

	// Directory where the snapshots are saved
	protected String snapshotDirectory;

	public Snapshot(String directory) {
		snapshotDirectory = directory;
	}

	public abstract void takeSnapShot();

	/**
	 * Create a unique directory for the current snapshot
	 */
	public String createDirectory(String aTraceName) {
		String dirName = "";
		File dir = new File(snapshotDirectory);

		// Check if the general directory exists
		if (!dir.exists()) {
			logger.debug("Snapshot directory (" + snapshotDirectory
					+ ") does not exist and will be created now.");

			// Create the general snapshot directory
			if (!dir.mkdirs()) {
				logger.error("Failed to create snapshot directory: "
						+ snapshotDirectory + ".");
			}
		}

		Date aDate = new Date(System.currentTimeMillis());
		String convertedDate = new SimpleDateFormat("dd-MM-yyyy_HHmmss_z")
				.format(aDate);

		String fileName = aTraceName + "_" + convertedDate;

		dirName = snapshotDirectory + "/" + fileName;
		dirName = Portability.normalize(dirName);

		// Create the specific snapshot directory
		dir = new File(dirName);
		if (!dir.mkdirs()) {
			logger.error("Failed to create snapshot directory: " + dirName + ".");
		}

		return dirName;
	}

	/**
	 * Create an image from the Figure given in argument
	 * 
	 * @param figure
	 *            Figure from which the image is created
	 * @param fileName
	 *            Path where to save the image
	 */
	public void createSnapshotFor(Figure figure, String fileName) {
		byte[] imageBytes = createImage(figure, SWT.IMAGE_PNG);

		if (imageBytes == null) {
			logger.debug("Image generation failed: snapshot image will not be created");
			return;
		}

		try {
			FileOutputStream out = new FileOutputStream(fileName);
			out.write(imageBytes);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generate the image
	 * 
	 * @param figure
	 *            Figure from which the image is created
	 * @param format
	 *            format of the generated image
	 * @return an array of bytes corresponding to an image
	 */
	private byte[] createImage(Figure figure, int format) {
		Device device = Display.getCurrent();
		Rectangle r = figure.getBounds();

		if (r.width <= 0 || r.height <= 0) {
			logger.debug("Size of figure is 0: stopping generation");
			return null;
		}

		ByteArrayOutputStream result = new ByteArrayOutputStream();

		Image image = null;
		GC gc = null;
		Graphics g = null;
		try {
			image = new Image(device, r.width, r.height);
			gc = new GC(image);
			g = new SWTGraphics(gc);
			g.translate(r.x * -1, r.y * -1);

			figure.paint(g);

			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { image.getImageData() };
			imageLoader.save(result, format);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (g != null) {
				g.dispose();
			}
			if (gc != null) {
				gc.dispose();
			}
			if (image != null) {
				image.dispose();
			}
		}
		return result.toByteArray();
	}

	/**
	 * Check that the snapshot directory is a valid one, i.e. does it exist and
	 * can it be written in
	 * 
	 * @param snapDirectory
	 *            path to the new snap directory
	 * @return true if valid, false otherwise
	 */
	public boolean checkSnapDirectoryValidity(String snapDirectory) {

		// Check the existence of the cache directory
		File dir = new File(snapDirectory);
		if (!dir.exists()) {
			logger.debug("Snapshot directory (" + snapDirectory
					+ ") does not exist and will be created now.");

			// Create the directory
			if (!dir.mkdirs()) {
				logger.error("Failed to create snapshot directory: "
						+ snapDirectory + ".");

				if (this.snapshotDirectory.isEmpty()) {
					logger.error("The current snapshot directory is still: "
							+ this.snapshotDirectory);
				}
				return false;
			}
		}

		// Check that we have at least the reading rights
		if (!dir.canWrite()) {
			logger.error("The application does not have the rights to write in the given directory: "
					+ snapDirectory + ".");

			if (this.snapshotDirectory.isEmpty()) {
				logger.error("No snapshot directory specified");
			} else {
				logger.error("The current snapshot directory is still: "
						+ this.snapshotDirectory);
			}
			return false;
		}

		return true;
	}
}
