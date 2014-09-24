/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Added drag and drop messages
 *******************************************************************************/

package fr.inria.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file
 *
 * @author Francois Chouinard
 * @version 1.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "fr.inria.linuxtools.internal.tmf.ui.project.handlers.messages"; //$NON-NLS-1$

    public static String DeleteDialog_Title;
    public static String DeleteTraceHandler_Message;
    public static String DeleteTraceHandler_Error;
    public static String DeleteExperimentHandler_Message;
    public static String DeleteExperimentHandler_Error;
    public static String DeleteFolderHandler_Message;
    public static String DeleteFolderHandler_Error;

    public static String SelectTraceTypeHandler_ErrorSelectingTrace;
    public static String SelectTraceTypeHandler_Title;
    public static String SelectTraceTypeHandler_TraceFailedValidation;
    public static String SelectTraceTypeHandler_TracesFailedValidation;
    public static String SelectTraceTypeHandler_InvalidTraceType;

    public static String DropAdapterAssistant_RenameTraceTitle;
    public static String DropAdapterAssistant_RenameTraceMessage;

    public static String SynchronizeTracesHandler_InitError;
    public static String SynchronizeTracesHandler_CopyProblem;
    public static String SynchronizeTracesHandler_WrongType;
    public static String SynchronizeTracesHandler_WrongTraceNumber;
    public static String SynchronizeTracesHandler_Title;
    public static String SynchronizeTracesHandler_Error;
    public static String SynchronizeTracesHandler_ErrorSynchingExperiment;
    public static String SynchronizeTracesHandler_ErrorSynchingForTrace;

    public static String AnalysisModule_Help;

    public static String TmfActionProvider_OpenWith;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
