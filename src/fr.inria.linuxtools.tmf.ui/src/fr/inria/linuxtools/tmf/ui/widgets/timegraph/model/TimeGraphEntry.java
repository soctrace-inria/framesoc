/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.widgets.timegraph.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An entry for use in the time graph views
 *
 * @since 2.1
 */
public class TimeGraphEntry implements ITimeGraphEntry {

    /** Entry's parent */
    private TimeGraphEntry fParent = null;

    /** List of child entries */
    private final List<TimeGraphEntry> fChildren = new CopyOnWriteArrayList<>();

    /** Name of this entry (text to show) */
    private String fName;
    private long fStartTime = -1;
    private long fEndTime = -1;
    private List<ITimeEvent> fEventList = new ArrayList<>();
    private List<ITimeEvent> fZoomedEventList = new ArrayList<>();

    /**
     * Constructor
     *
     * @param name
     *            The name of this entry
     * @param startTime
     *            The start time of this entry
     * @param endTime
     *            The end time of this entry
     */
    public TimeGraphEntry(String name, long startTime, long endTime) {
        fName = name;
        fStartTime = startTime;
        fEndTime = endTime;
    }

    // ---------------------------------------------
    // Getters and setters
    // ---------------------------------------------

    @Override
    public ITimeGraphEntry getParent() {
        return fParent;
    }

    /**
     * Sets the entry's parent
     *
     * @param entry The new parent entry
     * @Framesoc -- changed visibility from protected to public
     */
    public void setParent(TimeGraphEntry entry) {
        fParent = entry;
    }

    @Override
    public boolean hasChildren() {
        return fChildren.size() > 0;
    }

    @Override
    public List<TimeGraphEntry> getChildren() {
        return fChildren;
    }

    @Override
    public String getName() {
        return fName;
    }

    /**
     * Update the entry name
     *
     * @param name
     *            the updated entry name
     */
    public void setName(String name) {
        fName = name;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    /**
     * Updates the end time
     *
     * @param endTime
     *            the end time
     *
     * @since 3.0
     */
    public void updateEndTime(long endTime) {
        fEndTime = Math.max(endTime, fEndTime);
    }

    /**
     * Updates the start time
     *
     * @Framesoc
     *
     * @author Generoso Pagano
     *
     * @param startTime
     *            the start time
     */
    public void updateStartTime(long startTime) {
        fStartTime = Math.min(startTime, fStartTime);
    }

    @Override
    public boolean hasTimeEvents() {
        return true;
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator() {
        if (hasTimeEvents()) {
            return new EventIterator(fEventList, fZoomedEventList);
        }
        return null;
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
        if (!hasTimeEvents()) {
            return null;
        }
        return new EventIterator(fEventList, fZoomedEventList, startTime, stopTime);
    }

    /**
     * Add an event to this entry's event list. If necessary, update the start
     * and end time of the entry.
     *
     * @Framesoc
     * Modified by Generoso Pagano:
     * - the event is always added, even if the event list's last event starts
     *   at the same time as the event to add.
     * - the parents start/end time are updated as well
     *
     * @param event
     *            The time event to add
     */
    public void addEvent(ITimeEvent event) {
        long start = event.getTime();
        long end = start + event.getDuration();
        synchronized (fEventList) {
            fEventList.add(event);
            if (fStartTime == -1 || start < fStartTime) {
                fStartTime = start;
            }
            if (fEndTime == -1 || end > fEndTime) {
                fEndTime = end;
            }
        }
    }

    /**
     * Set the general event list of this entry.
     *
     * @param eventList
     *            The list of time events
     */
    public void setEventList(List<ITimeEvent> eventList) {
        if (eventList != null) {
            fEventList = new ArrayList<>(eventList);
        } else {
            fEventList = new ArrayList<>();
        }
    }

    /**
     * Set the zoomed event list of this entry.
     *
     * @param eventList
     *            The list of time events
     */
    public void setZoomedEventList(List<ITimeEvent> eventList) {
        if (eventList != null) {
            fZoomedEventList = new ArrayList<>(eventList);
        } else {
            fZoomedEventList = new ArrayList<>();
        }
    }

    /**
     * Add a child entry to this one
     *
     * @param child
     *            The child entry
     */
    public void addChild(TimeGraphEntry child) {
        child.fParent = this;
        fChildren.add(child);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + fName + ')';
    }

}
