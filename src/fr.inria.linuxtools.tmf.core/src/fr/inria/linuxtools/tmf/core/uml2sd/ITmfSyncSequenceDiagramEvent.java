/**********************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package fr.inria.linuxtools.tmf.core.uml2sd;

import fr.inria.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * <p>
 * Interface for synchronous sequence diagram events.
 * </p>
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public interface ITmfSyncSequenceDiagramEvent {

    /**
     * Returns Name of message.
     *
     * @return Name of message
     */
    String getName();

    /**
     * Returns name of sender of message.
     *
     * @return name of sender of message
     */
    String getSender();

    /**
     * Returns Name of receiver of message.
     *
     * @return Name of receiver of message
     */
    String getReceiver();

    /**
     * Returns Start time of message (i.e. send time).
     *
     * @return Start timestamp of message (i.e. send time)
     * @since 2.0
     */
    ITmfTimestamp getStartTime();
}
