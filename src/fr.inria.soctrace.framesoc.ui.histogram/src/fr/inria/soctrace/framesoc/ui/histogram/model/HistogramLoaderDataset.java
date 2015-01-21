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
package fr.inria.soctrace.framesoc.ui.histogram.model;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jfree.data.statistics.HistogramDataset;

import fr.inria.soctrace.framesoc.ui.histogram.loaders.DensityHistogramLoader;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;

/**
 * Dataset object used to share data between a histogram loader and a drawer thread.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class HistogramLoaderDataset {

	private HistogramDataset fDataset = new HistogramDataset();
	private TimeInterval fInterval = new TimeInterval(0, 0);
	private CountDownLatch fDoneLatch = new CountDownLatch(1);
	private boolean fComplete;
	private boolean fStop;
	private boolean fDirty;

	/**
	 * Set a snapshot for the dataset with the corresponding time interval.
	 * 
	 * @param snapshot
	 *            dataset values snapshot
	 * @param interval
	 *            time interval
	 */
	public synchronized void setSnapshot(double[] snapshot, TimeInterval interval) {
		fDataset = new HistogramDataset();
		fDataset.setType(DensityHistogramLoader.HISTOGRAM_TYPE);
		fDataset.addSeries(DensityHistogramLoader.DATASET_NAME, snapshot,
				DensityHistogramLoader.NUMBER_OF_BINS);
		fInterval.copy(interval);
		fDirty = true;
	}

	/**
	 * Get a snapshot of the dataset at a given time. The corresponding time interval is set in the
	 * passed parameter.
	 * 
	 * @param interval
	 *            output parameter, where the snapshot interval is set
	 * @return a snapshot of the dataset
	 */
	public synchronized HistogramDataset getSnapshot(TimeInterval interval) {
		interval.copy(fInterval);
		fDirty = false;
		return fDataset;
	}

	/**
	 * Set the complete flag.
	 * 
	 * The map is complete when all the requested interval has been loaded.
	 */
	public synchronized void setComplete() {
		fComplete = true;
		fDoneLatch.countDown();
	}

	/**
	 * Set the stop flag.
	 * 
	 * This flag means that something bad happened and the map won't be complete.
	 */
	public synchronized void setStop() {
		fStop = true;
		fDoneLatch.countDown();
	}

	/**
	 * 
	 * @return the complete flag
	 */
	public boolean isComplete() {
		return fComplete;
	}

	/**
	 * 
	 * @return the stop flag
	 */
	public boolean isStop() {
		return fStop;
	}

	/**
	 * Check if the map is dirty.
	 * 
	 * The map is dirty if a snapshot has been set and it has not been read yet.
	 * 
	 * @return the dirty flag
	 */
	public boolean isDirty() {
		return fDirty;
	}

	/**
	 * Wait until we are done or the timeout elapsed.
	 * 
	 * @param timeout
	 *            max wait timeout in milliseconds
	 * @param unit
	 *            timeout unit
	 * @return true if we are done, false if the timeout elapsed
	 */
	public boolean waitUntilDone(long timeout) {
		boolean done = false;
		try {
			done = fDoneLatch.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return done;
	}

	@Override
	public String toString() {
		return "HistogramLoaderDataset [fDataset=" + fDataset + ", fInterval=" + fInterval
				+ ", fComplete=" + fComplete + ", fStop=" + fStop + ", fDirty=" + fDirty + "]";
	}

}
