/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Francois Chouinard - Initial API and implementation
 * Francois Chouinard - Updated as per TMF Trace Model 1.0
 * Patrick Tasse - Updated for ranks in experiment location
 *******************************************************************************/

package fr.inria.linuxtools.internal.tmf.core.trace;

import java.nio.ByteBuffer;

import fr.inria.linuxtools.tmf.core.trace.location.ITmfLocation;


/**
 * The experiment location in TMF.
 * <p>
 * An experiment location is actually the set of locations of the traces it
 * contains. By setting the individual traces to their corresponding locations,
 * the experiment can be positioned to read the next chronological event.
 * <p>
 * It is the responsibility of the user the individual trace locations are valid
 * and that they are matched to the correct trace.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see TmfLocationArray
 */
public final class TmfExperimentLocation implements ITmfLocation {

    private final TmfLocationArray fLocation;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The standard constructor
     *
     * @param locations the set of trace locations
     */
    public TmfExperimentLocation(TmfLocationArray locations) {
        fLocation = locations;
    }

    /**
     * The copy constructor
     *
     * @param location the other experiment location
     */
    public TmfExperimentLocation(TmfExperimentLocation location) {
        this(location.getLocationInfo());
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        StringBuilder result = new StringBuilder("TmfExperimentLocation [");
        result.append(fLocation.toString());
        result.append("]");
        return result.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fLocation != null) ? fLocation.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TmfExperimentLocation other = (TmfExperimentLocation) obj;
        if (fLocation == null) {
            if (other.fLocation != null) {
                return false;
            }
        } else if (!fLocation.equals(other.fLocation)) {
            return false;
        }
        return true;
    }

    @Override
    public TmfLocationArray getLocationInfo() {
        return fLocation;
    }

    @Override
    public void serialize(ByteBuffer bufferOut) {
        ITmfLocation[] locations = fLocation.getLocations();
        long[] ranks = fLocation.getRanks();
        for (int i = 0; i < locations.length; ++i) {
            locations[i].serialize(bufferOut);
            bufferOut.putLong(ranks[i]);
        }
    }
}
