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
package fr.inria.soctrace.framesoc.ui.gantt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.lib.model.EventProducer;

/**
 * Interface for event drawers
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IEventDrawer {

	/**
	 * Set the map of event producers
	 * 
	 * @param producers
	 *            map between event producer id and event producers
	 */
	void setProducers(Map<Integer, EventProducer> producers);
	
	/**
	 * Draw a list of <code>ReducedEvent</code>.
	 * 
	 * @param events
	 *            a list of <code>ReducedEvent</code>
	 * @return the actual time interval it is possible to display in the viewer;
	 *         note that the end time of this interval should correspond to the
	 *         max start timestamp found so far.
	 */
	TimeInterval draw(List<ReducedEvent> events);

	/**
	 * Get all the links drawn so far
	 * 
	 * @return all the links
	 */
	ArrayList<ILinkEvent> getLinks();

	/**
	 * Get all the new entries added after the {@link #draw()} method is called.
	 * 
	 * @return the new root entries after a call to @link #draw()}
	 */
	ArrayList<TimeGraphEntry> getNewRootEntries();

	/**
	 * Tell if the viewer must be refreshed or simply redrawn after the
	 * {@link #draw()} method is called.
	 * 
	 * It should return true only if the draw method added new root time graph
	 * entries.
	 * 
	 * @return true if the viewer must be refreshed, false if it must be simply
	 *         redrawn
	 */
	boolean needRefresh();

	/**
	 * Release all the drawer resources and clean all internal data structures.
	 * This method should be called when we are finished using the loader.
	 */
	void release();

}
