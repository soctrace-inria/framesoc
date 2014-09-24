/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.TextStyle;
import org.osgi.framework.Bundle;

import fr.inria.linuxtools.tmf.core.analysis.IAnalysisModule;
import fr.inria.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import fr.inria.linuxtools.tmf.core.analysis.IAnalysisOutput;
import fr.inria.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import fr.inria.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Class for project elements of type analysis modules
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfAnalysisElement extends TmfProjectModelElement implements ITmfStyledProjectModelElement {

    private static final Styler ANALYSIS_CANT_EXECUTE_STYLER = new Styler() {
        @Override
        public void applyStyles(TextStyle textStyle) {
            textStyle.strikeout = true;
        }
    };

    private final String fAnalysisId;
    private boolean fCanExecute = true;

    /**
     * Constructor
     *
     * @param name
     *            Name of the analysis
     * @param resource
     *            The resource
     * @param parent
     *            Parent of the analysis
     * @param id
     *            The analysis module id
     */
    protected TmfAnalysisElement(String name, IResource resource, ITmfProjectModelElement parent, String id) {
        super(name, resource, parent);
        fAnalysisId = id;
        parent.addChild(this);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    void refreshChildren() {
        fCanExecute = true;

        /* Refresh the outputs of this analysis */
        Map<String, TmfAnalysisOutputElement> childrenMap = new HashMap<>();
        for (TmfAnalysisOutputElement output : getAvailableOutputs()) {
            childrenMap.put(output.getName(), output);
        }

        IAnalysisModuleHelper helper = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (helper == null) {
            deleteOutputs();
            return;
        }

        /** Get base path for resource */
        IPath path = getProject().getTracesFolder().getPath();
        if (fResource instanceof IFolder) {
            path = ((IFolder) fResource).getFullPath();
        }

        /*
         * We can get a list of available outputs once the analysis is
         * instantiated when the trace is opened
         */
        ITmfProjectModelElement parent = getParent();
        if (parent instanceof TmfCommonProjectElement) {
            ITmfTrace trace = ((TmfCommonProjectElement) parent).getTrace();
            if (trace == null) {
                deleteOutputs();
                return;
            }

            IAnalysisModule module = trace.getAnalysisModule(fAnalysisId);
            if (module == null) {
                deleteOutputs();
                /*
                 * Trace is opened, but the analysis is null, so it does not
                 * apply
                 */
                fCanExecute = false;
                return;
            }

            for (IAnalysisOutput output : module.getOutputs()) {
                TmfAnalysisOutputElement outputElement = childrenMap.remove(output.getName());
                if (outputElement == null) {
                    IFolder newresource = ResourcesPlugin.getWorkspace().getRoot().getFolder(path.append(output.getName()));
                    outputElement = new TmfAnalysisOutputElement(output.getName(), newresource, this, output);
                }
                outputElement.refreshChildren();
            }

        }
        /* Remove outputs that are not children of this analysis anymore */
        for (TmfAnalysisOutputElement output : childrenMap.values()) {
            removeChild(output);
        }
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public Styler getStyler() {
        if (!fCanExecute) {
            return ANALYSIS_CANT_EXECUTE_STYLER;
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Get the list of analysis output model elements under this analysis
     *
     * @return Array of analysis output elements
     */
    public List<TmfAnalysisOutputElement> getAvailableOutputs() {
        List<ITmfProjectModelElement> children = getChildren();
        List<TmfAnalysisOutputElement> outputs = new ArrayList<>();
        for (ITmfProjectModelElement child : children) {
            if (child instanceof TmfAnalysisOutputElement) {
                outputs.add((TmfAnalysisOutputElement) child);
            }
        }
        return outputs;
    }

    /**
     * Gets the analysis id of this module
     *
     * @return The analysis id
     */
    public String getAnalysisId() {
        return fAnalysisId;
    }

    /**
     * Gets the help message for this analysis
     *
     * @return The help message
     */
    public String getHelpMessage() {
        ITmfProjectModelElement parent = getParent();

        ITmfTrace trace = null;
        if (parent instanceof TmfTraceElement) {
            TmfTraceElement traceElement = (TmfTraceElement) parent;
            trace = traceElement.getTrace();
            if (trace != null) {
                IAnalysisModule module = trace.getAnalysisModule(fAnalysisId);
                if (module != null) {
                    return module.getHelpText(trace);
                }
            }
        }

        IAnalysisModuleHelper helper = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (helper == null) {
            return new String();
        }

        if (trace != null) {
            return helper.getHelpText(trace);
        }

        return helper.getHelpText();
    }

    /**
     * Gets the icon file name for the analysis
     *
     * @return The analysis icon file name
     */
    public String getIconFile() {
        IAnalysisModuleHelper helper = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (helper == null) {
            return null;
        }
        return helper.getIcon();
    }

    /**
     * Gets the bundle this analysis is from
     *
     * @return The analysis bundle
     */
    public Bundle getBundle() {
        IAnalysisModuleHelper helper = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (helper == null) {
            return null;
        }
        return helper.getBundle();
    }

    /** Delete all outputs under this analysis element */
    private void deleteOutputs() {
        for (TmfAnalysisOutputElement output : getAvailableOutputs()) {
            removeChild(output);
        }
    }

    /**
     * Make sure the trace this analysis is associated to is the currently
     * selected one
     */
    public void activateParent() {
        ITmfProjectModelElement parent = getParent();

        if (parent instanceof TmfTraceElement) {
            TmfTraceElement traceElement = (TmfTraceElement) parent;
            TmfOpenTraceHelper.openTraceFromElement(traceElement);
        }
    }

}
