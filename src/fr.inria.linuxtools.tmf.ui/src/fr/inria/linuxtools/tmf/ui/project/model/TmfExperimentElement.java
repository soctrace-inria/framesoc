/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Geneviève Bastien - Copied code to add/remove traces in this class
 *   Patrick Tasse - Close editors to release resources
 *   Geneviève Bastien - Experiment instantiated with trace type
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

import fr.inria.linuxtools.internal.tmf.ui.Activator;
import fr.inria.linuxtools.tmf.core.TmfCommonConstants;
import fr.inria.linuxtools.tmf.core.project.model.TmfTraceType;
import fr.inria.linuxtools.tmf.core.project.model.TraceTypeHelper;
import fr.inria.linuxtools.tmf.core.trace.TmfExperiment;
import fr.inria.linuxtools.tmf.ui.editors.TmfEventsEditor;
import fr.inria.linuxtools.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;

/**
 * Implementation of TMF Experiment Model Element.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 *
 */
public class TmfExperimentElement extends TmfCommonProjectElement implements IPropertySource2 {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // Property View stuff
    private static final String sfInfoCategory = "Info"; //$NON-NLS-1$
    private static final String sfName = "name"; //$NON-NLS-1$
    private static final String sfPath = "path"; //$NON-NLS-1$
    private static final String sfLocation = "location"; //$NON-NLS-1$
    private static final String sfFolderSuffix = "_exp"; //$NON-NLS-1$
    private static final String sfExperimentType = "type"; //$NON-NLS-1$

    private static final ReadOnlyTextPropertyDescriptor sfNameDescriptor = new ReadOnlyTextPropertyDescriptor(sfName, sfName);
    private static final ReadOnlyTextPropertyDescriptor sfPathDescriptor = new ReadOnlyTextPropertyDescriptor(sfPath, sfPath);
    private static final ReadOnlyTextPropertyDescriptor sfLocationDescriptor = new ReadOnlyTextPropertyDescriptor(sfLocation,
            sfLocation);
    private static final ReadOnlyTextPropertyDescriptor sfTypeDescriptor = new ReadOnlyTextPropertyDescriptor(sfExperimentType, sfExperimentType);

    private static final IPropertyDescriptor[] sfDescriptors = { sfNameDescriptor, sfPathDescriptor,
            sfLocationDescriptor, sfTypeDescriptor };

    static {
        sfNameDescriptor.setCategory(sfInfoCategory);
        sfPathDescriptor.setCategory(sfInfoCategory);
        sfLocationDescriptor.setCategory(sfInfoCategory);
        sfTypeDescriptor.setCategory(sfInfoCategory);
    }

    // The mapping of available trace type IDs to their corresponding
    // configuration element
    private static final Map<String, IConfigurationElement> sfTraceTypeAttributes = new HashMap<>();
    private static final Map<String, IConfigurationElement> sfTraceTypeUIAttributes = new HashMap<>();
    private static final Map<String, IConfigurationElement> sfTraceCategories = new HashMap<>();

    // ------------------------------------------------------------------------
    // Static initialization
    // ------------------------------------------------------------------------

    /**
     * Initialize statically at startup by getting extensions from the platform
     * extension registry.
     * @since 3.0
     */
    public static void init() {
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfTraceType.TMF_TRACE_TYPE_ID);
        for (IConfigurationElement ce : config) {
            String elementName = ce.getName();
            if (elementName.equals(TmfTraceType.EXPERIMENT_ELEM)) {
                String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                sfTraceTypeAttributes.put(traceTypeId, ce);
            } else if (elementName.equals(TmfTraceType.CATEGORY_ELEM)) {
                String categoryId = ce.getAttribute(TmfTraceType.ID_ATTR);
                sfTraceCategories.put(categoryId, ce);
            }
        }

        /*
         * Read the corresponding tmf.ui "tracetypeui" extension point for this
         * trace type, if it exists.
         */
        config = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfTraceTypeUIUtils.TMF_TRACE_TYPE_UI_ID);
        for (IConfigurationElement ce : config) {
            String elemName = ce.getName();
            if (TmfTraceTypeUIUtils.EXPERIMENT_ELEM.equals(elemName)) {
                String traceType = ce.getAttribute(TmfTraceTypeUIUtils.TRACETYPE_ATTR);
                sfTraceTypeUIAttributes.put(traceType, ce);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name The name of the experiment
     * @param folder The folder reference
     * @param parent The experiment folder reference.
     */
    public TmfExperimentElement(String name, IFolder folder, TmfExperimentFolder parent) {
        super(name, folder, parent);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public IFolder getResource() {
        return (IFolder) fResource;
    }

    @Override
    void refreshChildren() {
        IFolder folder = getResource();

        /* Update the trace children of this experiment */
        // Get the children from the model
        Map<String, ITmfProjectModelElement> childrenMap = new HashMap<>();
        for (TmfTraceElement trace : getTraces()) {
            childrenMap.put(trace.getElementPath(), trace);
        }

        List<IResource> members = getTraceResources();
        for (IResource resource : members) {
            String name = resource.getName();
            String elementPath = resource.getFullPath().makeRelativeTo(folder.getFullPath()).toString();
            ITmfProjectModelElement element = childrenMap.get(elementPath);
            if (element instanceof TmfTraceElement) {
                childrenMap.remove(elementPath);
            } else {
                element = new TmfTraceElement(name, resource, this);
            }
        }

        // Cleanup dangling children from the model
        for (ITmfProjectModelElement danglingChild : childrenMap.values()) {
            removeChild(danglingChild);
        }

        /* Update the analysis under this experiment */
        super.refreshChildren();
    }

    private List<IResource> getTraceResources() {
        IFolder folder = getResource();
        final List<IResource> list = new ArrayList<>();
        try {
            folder.accept(new IResourceProxyVisitor() {
                @Override
                public boolean visit(IResourceProxy resource) throws CoreException {
                    if (resource.isLinked()) {
                        list.add(resource.requestResource());
                    }
                    return true;
                }
            }, IResource.NONE);
        } catch (CoreException e) {
        }
        return list;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Refreshes the trace type filed by reading the trace type persistent
     * property of the resource reference.
     *
     * If trace type is null after refresh, set it to the generic trace type
     * (for seamless upgrade)
     */
    @Override
    public void refreshTraceType() {
        super.refreshTraceType();
        if (getTraceType() == null) {
            IConfigurationElement ce = TmfTraceType.getTraceAttributes(TmfTraceType.DEFAULT_EXPERIMENT_TYPE);
            if (ce != null) {
                try {
                    IFolder experimentFolder = getResource();
                    experimentFolder.setPersistentProperty(TmfCommonConstants.TRACETYPE, ce.getAttribute(TmfTraceType.ID_ATTR));
                    super.refreshTraceType();
                } catch (InvalidRegistryObjectException | CoreException e) {
                }
            }
        }
    }

    /**
     * Returns a list of TmfTraceElements contained in this experiment.
     * @return a list of TmfTraceElements
     */
    @Override
    public List<TmfTraceElement> getTraces() {
        List<ITmfProjectModelElement> children = getChildren();
        List<TmfTraceElement> traces = new ArrayList<>();
        for (ITmfProjectModelElement child : children) {
            if (child instanceof TmfTraceElement) {
                traces.add((TmfTraceElement) child);
            }
        }
        return traces;
    }

    /**
     * Adds a trace to the experiment
     *
     * @param trace The trace element to add
     * @since 2.0
     */
    public void addTrace(TmfTraceElement trace) {
        /**
         * Create a link to the actual trace and set the trace type
         */
        IFolder experiment = getResource();
        IResource resource = trace.getResource();
        IPath location = resource.getLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            String traceTypeId = trace.getResource().getPersistentProperty(TmfCommonConstants.TRACETYPE);
            TraceTypeHelper traceType = TmfTraceType.getTraceType(traceTypeId);

            if (resource instanceof IFolder) {
                IFolder folder = experiment.getFolder(trace.getElementPath());
                TraceUtils.createFolder((IFolder) folder.getParent(), new NullProgressMonitor());
                IStatus result = workspace.validateLinkLocation(folder, location);
                if (result.isOK() || result.matches(IStatus.INFO | IStatus.WARNING)) {
                    folder.createLink(location, IResource.REPLACE, null);
                    if (traceType != null) {
                        TmfTraceTypeUIUtils.setTraceType(folder, traceType);
                    }

                } else {
                    Activator.getDefault().logError("Error creating link. Invalid trace location " + location); //$NON-NLS-1$
                }
            } else {
                IFile file = experiment.getFile(trace.getElementPath());
                TraceUtils.createFolder((IFolder) file.getParent(), new NullProgressMonitor());
                IStatus result = workspace.validateLinkLocation(file, location);
                if (result.isOK() || result.matches(IStatus.INFO | IStatus.WARNING)) {
                    file.createLink(location, IResource.REPLACE, null);
                    if (traceType != null) {
                        TmfTraceTypeUIUtils.setTraceType(file, traceType);
                    }
                } else {
                    Activator.getDefault().logError("Error creating link. Invalid trace location " + location); //$NON-NLS-1$
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating link to location " + location, e); //$NON-NLS-1$
        }

    }

    /**
     * Removes a trace from an experiment
     *
     * @param trace The trace to remove
     * @throws CoreException exception
     * @since 2.0
     */
    public void removeTrace(TmfTraceElement trace) throws CoreException {

        // Close the experiment if open
        closeEditors();

        /* Finally, remove the trace from experiment*/
        removeChild(trace);
        deleteTraceResource(trace.getResource());
        deleteSupplementaryResources();
    }

    private void deleteTraceResource(IResource resource) throws CoreException {
        resource.delete(true, null);
        IContainer parent = resource.getParent();
        // delete empty folders up to the parent experiment folder
        if (!parent.equals(getResource()) && parent.members().length == 0) {
            deleteTraceResource(parent);
        }
    }

    @Override
    public IFile createBookmarksFile() throws CoreException {
        return createBookmarksFile(getProject().getExperimentsFolder().getResource(), TmfExperiment.class.getCanonicalName());
    }

    @Override
    public String getEditorId() {
        /* See if a default editor was set for this experiment type */
        if (getTraceType() != null) {
            IConfigurationElement ce = sfTraceTypeUIAttributes.get(getTraceType());
            if (ce != null) {
                IConfigurationElement[] defaultEditorCE = ce.getChildren(TmfTraceTypeUIUtils.DEFAULT_EDITOR_ELEM);
                if (defaultEditorCE.length == 1) {
                    return defaultEditorCE[0].getAttribute(TmfTraceType.ID_ATTR);
                }
            }
        }

        /* No default editor, try to find a common editor for all traces */
        final List<TmfTraceElement> traceEntries = getTraces();
        String commonEditorId = null;

        for (TmfTraceElement element : traceEntries) {
            // If all traces use the same editorId, use it, otherwise use the
            // default
            final String editorId = element.getEditorId();
            if (commonEditorId == null) {
                commonEditorId = (editorId != null) ? editorId : TmfEventsEditor.ID;
            } else if (!commonEditorId.equals(editorId)) {
                commonEditorId = TmfEventsEditor.ID;
            }
        }
        return null;
    }

    /**
     * Instantiate a {@link TmfExperiment} object based on the experiment type
     * and the corresponding extension.
     *
     * @return the {@link TmfExperiment} or <code>null</code> for an error
     * @since 3.0
     */
    @Override
    public TmfExperiment instantiateTrace() {
        try {

            // make sure that supplementary folder exists
            refreshSupplementaryFolder();

            if (getTraceType() != null) {

                IConfigurationElement ce = sfTraceTypeAttributes.get(getTraceType());
                if (ce == null) {
                    return null;
                }
                TmfExperiment experiment = (TmfExperiment) ce.createExecutableExtension(TmfTraceType.EXPERIMENT_TYPE_ATTR);
                return experiment;
            }
        } catch (CoreException e) {
            Activator.getDefault().logError(NLS.bind(Messages.TmfExperimentElement_ErrorInstantiatingTrace, getName()), e);
        }
        return null;
    }

    @Override
    public String getTypeName() {
        return Messages.TmfExperimentElement_TypeName;
    }

    // ------------------------------------------------------------------------
    // IPropertySource2
    // ------------------------------------------------------------------------

    @Override
    public Object getEditableValue() {
        return null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return Arrays.copyOf(sfDescriptors, sfDescriptors.length);
    }

    @Override
    public Object getPropertyValue(Object id) {

        if (sfName.equals(id)) {
            return getName();
        }

        if (sfPath.equals(id)) {
            return getPath().toString();
        }

        if (sfLocation.equals(id)) {
            return getLocation().toString();
        }

        if (sfExperimentType.equals(id)) {
            if (getTraceType() != null) {
                IConfigurationElement ce = sfTraceTypeAttributes.get(getTraceType());
                if (ce == null) {
                    return ""; //$NON-NLS-1$
                }
                String categoryId = ce.getAttribute(TmfTraceType.CATEGORY_ATTR);
                if (categoryId != null) {
                    IConfigurationElement category = sfTraceCategories.get(categoryId);
                    if (category != null) {
                        return category.getAttribute(TmfTraceType.NAME_ATTR) + ':' + ce.getAttribute(TmfTraceType.NAME_ATTR);
                    }
                }
                return ce.getAttribute(TmfTraceType.NAME_ATTR);
            }
        }

        return null;
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
    }

    @Override
    public boolean isPropertyResettable(Object id) {
        return false;
    }

    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

    /**
     * Return the suffix for resource names
     * @return The folder suffix
     */
    @Override
    public String getSuffix() {
        return sfFolderSuffix;
    }

}
