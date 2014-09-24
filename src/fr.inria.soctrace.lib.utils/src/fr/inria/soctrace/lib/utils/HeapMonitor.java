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
package fr.inria.soctrace.lib.utils;

/**
 * Heap monitor thread. It writes its results into the History log.
 * 
 * <p>
 * Notes:
 * <ul>
 * <li> this thread has a fixed negligible heap occupation
 * <li> warning: Runtime freeMemory() does not get updated immediately when 
 *      totalMemory() size changes, so this thread should be used with a start
 *      heap size equal to the maximum heap size. E.g. -Xms1024m -Xmx1024m
 * </ul>
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class HeapMonitor extends Thread {
	
	/**
	 * Static variable used to assign monitor IDs.
	 */
	private static long monitorId = 0;
	
	/**
	 * Monitor name in logs
	 */
	private final String monitorName;
	
	/**
	 * Sampling period in milliseconds
	 */
	private long period = 1000;
	
	/**
	 * Stop flag
	 */
	private boolean end = false;
	
	/**
	 * Runtime instance
	 */
	private Runtime runtime = Runtime.getRuntime();
	
	/**
	 * Constructor. Increments the static monitorId counter.
	 * @param period sampling period in milliseconds
	 */
	public HeapMonitor(long period) {
		monitorName = "(HeapMonitor#"+ monitorId++ +") ";
		this.period = period;
		logHeap();
	}
	
	/**
	 * Monitor heap size until stop flag is set.
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(period);
				logHeap();
			} catch (InterruptedException e) {
				;
			}
			if (end) break;
		}
	}

	/**
	 * Set the stop flag to true.
	 */
	public void end() {
		this.interrupt();
		end = true;
	}
	
	/**
	 * Log the used heap size on demand
	 */
	public void logHeap() {
		History.add(monitorName + "USED_HEAP " + getUsedHeap());
	}

	private String getUsedHeap() {
		return (runtime.totalMemory() - runtime.freeMemory()) + "";
	}
}
