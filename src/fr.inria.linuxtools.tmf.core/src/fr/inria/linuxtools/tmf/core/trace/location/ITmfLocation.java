/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package fr.inria.linuxtools.tmf.core.trace.location;

import java.nio.ByteBuffer;

/**
 * The generic trace location in TMF.
 * <p>
 * An ITmfLocation is the equivalent of a random-access file position, holding
 * enough information to allow the positioning of the trace 'pointer' to read an
 * arbitrary event.
 * <p>
 * This location is trace-specific, must be comparable and immutable.
 *
 * @author Francois Chouinard
 * @since 3.0
 */
public interface ITmfLocation {

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Returns the concrete trace location information
     *
     * @return the location information
     * @since 2.0
     */
    Comparable<?> getLocationInfo();

    /**
     * Write the location to the ByteBuffer so that it can be saved to disk.
     * @param bufferOut the buffer to write to
     *
     * @since 3.0
     */
    void serialize(ByteBuffer bufferOut);
}
