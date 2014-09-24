/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Patrick Tasse - Add support for source location
 *******************************************************************************/
package fr.inria.linuxtools.tmf.core;

import org.eclipse.core.runtime.QualifiedName;

/**
 *  This class provides a common container for TMF constants.
 *
 * @version 1.0
 *  @author Bernd Hufmann
 */
public class TmfCommonConstants {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The trace type ID persistent property of a trace resource.
     */
    public static final QualifiedName TRACETYPE = new QualifiedName("fr.inria.linuxtools.tmf", "tracetype.id"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * The source location persistent property of a trace resource.
     * @since 3.0
     */
    public static final QualifiedName SOURCE_LOCATION = new QualifiedName("fr.inria.linuxtools.tmf", "source.location"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * The supplementary folder name persistent property of a trace resource.
     */
    public static final QualifiedName TRACE_SUPPLEMENTARY_FOLDER = new QualifiedName("fr.inria.linuxtools.tmf", "trace.suppl.folder"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * The name of the parent folder for storing trace specific supplementary data. Each trace will have a sub-directory underneath with folder name equal to the trace name.
     * @since 3.0
     */
    public static final String TRACE_SUPPLEMENTARY_FOLDER_NAME = ".tracing"; //$NON-NLS-1$

    /**
     * The name of the default project that can be created under various
     * conditions when there is no tracing project in the workspace.
     *
     * @since 2.2
     */
    public static final String DEFAULT_TRACE_PROJECT_NAME = Messages.DefaultTraceProjectName;

}
