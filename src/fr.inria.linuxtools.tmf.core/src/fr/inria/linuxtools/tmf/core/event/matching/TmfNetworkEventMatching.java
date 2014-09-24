/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package fr.inria.linuxtools.tmf.core.event.matching;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.inria.linuxtools.tmf.core.event.ITmfEvent;
import fr.inria.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * This class matches events typically network-style, ie. where some events are
 * 'send' events and the other 'receive' events or out/in events
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfNetworkEventMatching extends TmfEventMatching {

    /**
     * Hashtables for unmatches incoming events
     */
    private final Map<ITmfTrace, Map<List<Object>, ITmfEvent>> fUnmatchedIn = new LinkedHashMap<>();

    /**
     * Hashtables for unmatches outgoing events
     */
    private final Map<ITmfTrace, Map<List<Object>, ITmfEvent>> fUnmatchedOut = new LinkedHashMap<>();

    /**
     * Enum for in and out types
     */
    public enum Direction {
        /**
         * The event is a 'receive' type of event
         */
        IN,
        /**
         * The event is a 'send' type of event
         */
        OUT,
    }

    /**
     * Constructor with multiple traces and match processing object
     *
     * @param traces
     *            The set of traces for which to match events
     */
    public TmfNetworkEventMatching(Collection<ITmfTrace> traces) {
        this(traces, new TmfEventMatches());
    }

    /**
     * Constructor with multiple traces and match processing object
     *
     * @param traces
     *            The set of traces for which to match events
     * @param tmfEventMatches
     *            The match processing class
     */
    public TmfNetworkEventMatching(Collection<ITmfTrace> traces, IMatchProcessingUnit tmfEventMatches) {
        super(traces, tmfEventMatches);
    }

    /**
     * Method that initializes any data structure for the event matching
     */
    @Override
    public void initMatching() {
        // Initialize the matching infrastructure (unmatched event lists)
        fUnmatchedIn.clear();
        fUnmatchedOut.clear();
        for (ITmfTrace trace : getTraces()) {
            fUnmatchedIn.put(trace, new HashMap<List<Object>, ITmfEvent>());
            fUnmatchedOut.put(trace, new HashMap<List<Object>, ITmfEvent>());
        }
        super.initMatching();
    }

    /**
     * Function that counts the events in a hashtable.
     *
     * @param tbl
     *            The table to count events for
     * @return The number of events
     */
    protected int countEvents(Map<List<Object>, ITmfEvent> tbl) {
        return tbl.size();
    }

    @Override
    protected MatchingType getMatchingType() {
        return MatchingType.NETWORK;
    }

    @Override
    public void matchEvent(ITmfEvent event, ITmfTrace trace) {
        if (!(getEventDefinition(event.getTrace()) instanceof ITmfNetworkMatchDefinition)) {
            return;
        }
        ITmfNetworkMatchDefinition def = (ITmfNetworkMatchDefinition) getEventDefinition(event.getTrace());

        Direction evType = def.getDirection(event);
        if (evType == null) {
            return;
        }

        /* Get the event's unique fields */
        List<Object> eventKey = def.getUniqueField(event);
        Map<ITmfTrace, Map<List<Object>, ITmfEvent>> unmatchedTbl, companionTbl;

        /* Point to the appropriate table */
        switch (evType) {
        case IN:
            unmatchedTbl = fUnmatchedIn;
            companionTbl = fUnmatchedOut;
            break;
        case OUT:
            unmatchedTbl = fUnmatchedOut;
            companionTbl = fUnmatchedIn;
            break;
        default:
            return;
        }

        boolean found = false;
        TmfEventDependency dep = null;
        /* Search for the event in the companion table */
        for (Map<List<Object>, ITmfEvent> map : companionTbl.values()) {
            if (map.containsKey(eventKey)) {
                found = true;
                ITmfEvent companionEvent = map.get(eventKey);

                /* Remove the element from the companion table */
                map.remove(eventKey);

                /* Create the dependency object */
                switch (evType) {
                case IN:
                    dep = new TmfEventDependency(companionEvent, event);
                    break;
                case OUT:
                    dep = new TmfEventDependency(event, companionEvent);
                    break;
                default:
                    break;

                }
            }
        }

        /*
         * If no companion was found, add the event to the appropriate unMatched
         * lists
         */
        if (found) {
            getProcessingUnit().addMatch(dep);
        } else {
            /*
             * If an event is already associated with this key, do not add it
             * again, we keep the first event chronologically, so if its match
             * is eventually found, it is associated with the first send or
             * receive event. At best, it is a good guess, at worst, the match
             * will be too far off to be accurate. Too bad!
             *
             * TODO: maybe instead of just one event, we could have a list of
             * events as value for the unmatched table. Not necessary right now
             * though
             */
            if (!unmatchedTbl.get(trace).containsKey(eventKey)) {
                unmatchedTbl.get(trace).put(eventKey, event);
            }
        }

    }

    /**
     * Prints stats from the matching
     *
     * @return string of statistics
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        final String cr = System.getProperty("line.separator");
        StringBuilder b = new StringBuilder();
        b.append(getProcessingUnit());
        int i = 0;
        for (ITmfTrace trace : getTraces()) {
            b.append("Trace " + i++ + ":" + cr +
                    "  " + countEvents(fUnmatchedIn.get(trace)) + " unmatched incoming events" + cr +
                    "  " + countEvents(fUnmatchedOut.get(trace)) + " unmatched outgoing events" + cr);
        }

        return b.toString();
    }

}
