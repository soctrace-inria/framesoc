/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package fr.inria.linuxtools.tmf.core.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fr.inria.linuxtools.tmf.core.event.ITmfEvent;
import fr.inria.linuxtools.tmf.core.event.ITmfLostEvent;
import fr.inria.linuxtools.tmf.core.request.ITmfEventRequest;
import fr.inria.linuxtools.tmf.core.request.TmfEventRequest;
import fr.inria.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import fr.inria.linuxtools.tmf.core.timestamp.TmfTimeRange;
import fr.inria.linuxtools.tmf.core.timestamp.TmfTimestamp;
import fr.inria.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Implementation of ITmfStatistics which uses event requests to the trace to
 * retrieve its information.
 *
 * There is almost no setup time, but queries themselves are longer than with a
 * TmfStateStatistics. Queries are O(n * m), where n is the size of the trace,
 * and m is the portion of the trace covered by the selected interval.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfEventsStatistics implements ITmfStatistics {

    /* All timestamps should be stored in nanoseconds in the statistics backend */
    private static final int SCALE = ITmfTimestamp.NANOSECOND_SCALE;

    private final ITmfTrace trace;

    /* Event request objects for the time-range request. */
    private StatsTotalRequest totalRequest = null;
    private StatsPerTypeRequest perTypeRequest = null;

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which we are building the statistics
     */
    public TmfEventsStatistics(ITmfTrace trace) {
        this.trace = trace;
    }

    @Override
    public void dispose() {
        cancelOngoingRequests();
    }

    @Override
    public List<Long> histogramQuery(long start, long end, int nb) {
        final long[] borders = new long[nb];
        final long increment = (end - start) / nb;

        long curTime = start;
        for (int i = 0; i < nb; i++) {
            borders[i] = curTime;
            curTime += increment;
        }

        HistogramQueryRequest req = new HistogramQueryRequest(borders, end);
        sendAndWait(req);

        List<Long> results = new LinkedList<>(req.getResults());
        return results;

    }

    private synchronized void cancelOngoingRequests() {
        if (totalRequest != null && totalRequest.isRunning()) {
            totalRequest.cancel();
        }
        if (perTypeRequest != null && perTypeRequest.isRunning()) {
            perTypeRequest.cancel();
        }
    }

    @Override
    public long getEventsTotal() {
        StatsTotalRequest request = new StatsTotalRequest(trace, TmfTimeRange.ETERNITY);
        sendAndWait(request);

        long total = request.getResult();
        return total;
    }

    @Override
    public Map<String, Long> getEventTypesTotal() {
        StatsPerTypeRequest request = new StatsPerTypeRequest(trace, TmfTimeRange.ETERNITY);
        sendAndWait(request);

        Map<String, Long> stats =  request.getResults();
        return stats;
    }

    @Override
    public long getEventsInRange(long start, long end) {
        ITmfTimestamp startTS = new TmfTimestamp(start, SCALE);
        ITmfTimestamp endTS = new TmfTimestamp(end, SCALE);
        TmfTimeRange range = new TmfTimeRange(startTS, endTS);

        StatsTotalRequest request = new StatsTotalRequest(trace, range);
        sendAndWait(request);

        long total =  request.getResult();
        return total;
    }

    @Override
    public Map<String, Long> getEventTypesInRange(long start, long end) {
        ITmfTimestamp startTS = new TmfTimestamp(start, SCALE);
        ITmfTimestamp endTS = new TmfTimestamp(end, SCALE);
        TmfTimeRange range = new TmfTimeRange(startTS, endTS);

        StatsPerTypeRequest request = new StatsPerTypeRequest(trace, range);
        sendAndWait(request);

        Map<String, Long> stats =  request.getResults();
        return stats;
    }

    private void sendAndWait(TmfEventRequest request) {
        trace.sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Event request to get the total number of events
     */
    private class StatsTotalRequest extends TmfEventRequest {

        /* Total number of events the request has found */
        private long total;

        public StatsTotalRequest(ITmfTrace trace, TmfTimeRange range) {
            super(trace.getEventType(), range, 0, ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);
            total = 0;
        }

        public long getResult() {
            return total;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (!(event instanceof ITmfLostEvent) && event.getTrace() == trace) {
                total += 1;
            }
        }
    }


    /**
     * Event request to get the counts per event type
     */
    private class StatsPerTypeRequest extends TmfEventRequest {

        /* Map in which the results are saved */
        private final Map<String, Long> stats;

        public StatsPerTypeRequest(ITmfTrace trace, TmfTimeRange range) {
            super(trace.getEventType(), range, 0, ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);
            this.stats = new HashMap<>();
        }

        public Map<String, Long> getResults() {
            return stats;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event.getTrace() == trace) {
                String eventType = event.getType().getName();
                /*
                 * Special handling for lost events: instead of counting just
                 * one, we will count how many actual events it represents.
                 */
                if (event instanceof ITmfLostEvent) {
                    ITmfLostEvent le = (ITmfLostEvent) event;
                    incrementStats(eventType, le.getNbLostEvents());
                    return;
                }

                /* For standard event types, just increment by one */
                incrementStats(eventType, 1L);
            }
        }

        private void incrementStats(String key, long count) {
            if (stats.containsKey(key)) {
                long curValue = stats.get(key);
                stats.put(key, curValue + count);
            } else {
                stats.put(key, count);
            }
        }
    }

    /**
     * Event request for histogram queries. It is much faster to do one event
     * request then set the results accordingly than doing thousands of them one
     * by one.
     */
    private class HistogramQueryRequest extends TmfEventRequest {

        /** Map of <borders, number of events> */
        private final TreeMap<Long, Long> results;

        /**
         * New histogram request
         *
         * @param borders
         *            The array of borders (not including the end time). The
         *            first element should be the start time of the queries.
         * @param endTime
         *            The end time of the query. Not used in the results map,
         *            but we need to know when to stop the event request.
         */
        public HistogramQueryRequest(long[] borders, long endTime) {
            super(trace.getEventType(),
                    new TmfTimeRange(
                            new TmfTimestamp(borders[0], SCALE),
                            new TmfTimestamp(endTime, SCALE)),
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);

            /* Prepare the results map, with all counts at 0 */
            results = new TreeMap<>();
            for (long border : borders) {
                results.put(border, 0L);
            }
        }

        public Collection<Long> getResults() {
            return results.values();
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);
            if (event.getTrace() == trace) {
                long ts = event.getTimestamp().normalize(0, SCALE).getValue();
                Long key = results.floorKey(ts);
                if (key != null) {
                    incrementValue(key);
                }
            }
        }

        private void incrementValue(Long key) {
            long value = results.get(key);
            value++;
            results.put(key, value);
        }
    }

}
