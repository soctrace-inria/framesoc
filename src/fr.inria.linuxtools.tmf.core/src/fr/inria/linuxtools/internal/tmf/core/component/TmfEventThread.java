/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Update handling of suspend and resume
 *******************************************************************************/

package fr.inria.linuxtools.internal.tmf.core.component;

import fr.inria.linuxtools.internal.tmf.core.Activator;
import fr.inria.linuxtools.internal.tmf.core.TmfCoreTracer;
import fr.inria.linuxtools.tmf.core.component.ITmfEventProvider;
import fr.inria.linuxtools.tmf.core.component.TmfEventProvider;
import fr.inria.linuxtools.tmf.core.event.ITmfEvent;
import fr.inria.linuxtools.tmf.core.request.ITmfEventRequest;
import fr.inria.linuxtools.tmf.core.request.ITmfEventRequest.ExecutionType;
import fr.inria.linuxtools.tmf.core.trace.ITmfContext;

/**
 * Provides the core event request processor. It also has support for suspending
 * and resuming a request in a thread-safe manner.
 *
 * @author Francois Chouinard
 * @version 1.0
 */
public class TmfEventThread implements Runnable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The event provider
     */
    private final TmfEventProvider fProvider;

    /**
     * The wrapped event request
     */
    private final ITmfEventRequest fRequest;

    /**
     * The request execution priority
     */
    private final ExecutionType   fExecType;

    /**
     * The wrapped thread (if applicable)
     */
    private final TmfEventThread  fThread;

    /**
     * The thread execution state
     */
    private volatile boolean isCompleted = false;

    /** The synchronization object */
    private final Object fSynchObject = new Object();

    /** The flag for suspending a thread */
    private volatile boolean fIsPaused = false;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Basic constructor
     *
     * @param provider the event provider
     * @param request the request to process
     */
    public TmfEventThread(TmfEventProvider provider, ITmfEventRequest request) {
        assert provider != null;
        assert request  != null;
        fProvider = provider;
        fRequest  = request;
        fExecType = request.getExecType();
        fThread   = null;
    }

    /**
     * Wrapper constructor
     *
     * @param thread the thread to wrap
     */
    public TmfEventThread(TmfEventThread thread) {
        fProvider = thread.fProvider;
        fRequest  = thread.fRequest;
        fExecType = thread.fExecType;
        fThread   = thread;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return The wrapped thread
     */
    public TmfEventThread getThread() {
        return fThread;
    }

    /**
     * @return The event provider
     */
    public ITmfEventProvider getProvider() {
        return fProvider;
    }

    /**
     * @return The event request
     */
    public ITmfEventRequest getRequest() {
        return fRequest;
    }

    /**
     * @return The request execution priority
     */
    public ExecutionType getExecType() {
        return fExecType;
    }

    /**
     * @return The request execution state
     */
    public boolean isRunning() {
        return fRequest.isRunning() && !isPaused();
    }

    /**
     * @return The request execution state
     */
    public boolean isPaused() {
        return fIsPaused;
    }

    /**
     * @return The request execution state
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    // ------------------------------------------------------------------------
    // Runnable
    // ------------------------------------------------------------------------

    @Override
    public void run() {

        TmfCoreTracer.traceRequest(fRequest, "is being serviced by " + fProvider.getName()); //$NON-NLS-1$

        // Extract the generic information
        fRequest.start();
        int nbRequested = fRequest.getNbRequested();
        int nbRead = 0;
        isCompleted = false;

        // Initialize the execution
        ITmfContext context = fProvider.armRequest(fRequest);
        if (context == null) {
            fRequest.cancel();
            return;
        }

        try {
            // Get the ordered events
            ITmfEvent event = fProvider.getNext(context);
            TmfCoreTracer.traceRequest(fRequest, "read first event"); //$NON-NLS-1$

            while (event != null && !fProvider.isCompleted(fRequest, event, nbRead)) {

                TmfCoreTracer.traceEvent(fProvider, fRequest, event);
                if (fRequest.getDataType().isInstance(event)) {
                    fRequest.handleData(event);
                }

                // Pause execution if requested

                while (fIsPaused) {
                    synchronized (fSynchObject) {
                        try {
                            fSynchObject.wait();
                        } catch (InterruptedException e) {
                            // continue
                        }
                    }
                }

                // To avoid an unnecessary read passed the last event requested
                if (++nbRead < nbRequested) {
                    event = fProvider.getNext(context);
                }
            }

            isCompleted = true;

            if (fRequest.isCancelled()) {
                fRequest.cancel();
            } else {
                fRequest.done();
            }

        } catch (Exception e) {
            Activator.logError("Error in " + fProvider.getName() + " handling " + fRequest, e); //$NON-NLS-1$ //$NON-NLS-2$
            fRequest.fail();
        }

        // Cleanup
        context.dispose();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Suspend the thread
     */
    public void suspend() {
        fIsPaused = true;
        TmfCoreTracer.traceRequest(fRequest, "SUSPENDED"); //$NON-NLS-1$
    }

    /**
     * Resume the thread
     */
    public void resume() {
        fIsPaused = false;
        synchronized (fSynchObject) {
            fSynchObject.notifyAll();
        }
        TmfCoreTracer.traceRequest(fRequest, "RESUMED"); //$NON-NLS-1$
    }

    /**
     * Cancel the request
     */
    public void cancel() {
        if (!fRequest.isCompleted()) {
            fRequest.cancel();
        }
    }
}
