/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added supplementary files/folder handling
 *   Patrick Tasse - Refactor resource change listener
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.project.model;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

import fr.inria.linuxtools.internal.tmf.ui.Activator;
import fr.inria.linuxtools.tmf.core.TmfCommonConstants;

/**
 * The implementation of the base TMF project model element. It provides default implementation
 * of the <code>ITmfProjectModelElement</code> interface.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class TmfProjectModelElement implements ITmfProjectModelElement {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final String fName;
    /**
     * The project model element resource.
     */
    protected final IResource fResource;
    /**
     * The project model resource location (URI)
     */
    protected final URI fLocation;
    /**
     * The project model path of a resource.
     */
    protected final IPath fPath;
    private final ITmfProjectModelElement fParent;
    /**
     * The list of children elements.
     */
    protected final List<ITmfProjectModelElement> fChildren;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * Creates a base project model element.
     * @param name The name of the element.
     * @param resource The element resource.
     * @param parent The parent model element.
     */
    protected TmfProjectModelElement(String name, IResource resource, ITmfProjectModelElement parent) {
        fName = name;
        fResource = resource;
        fPath = resource.getFullPath();
        fLocation = new File(resource.getLocationURI()).toURI();
        fParent = parent;
        fChildren = new CopyOnWriteArrayList<>();
    }

    // ------------------------------------------------------------------------
    // ITmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public IResource getResource() {
        return fResource;
    }

    @Override
    public IPath getPath() {
        return fPath;
    }

    @Override
    public URI getLocation() {
        return fLocation;
    }

    /**
     * @since 3.0
     */
    @Override
    public TmfProjectElement getProject() {
        return fParent.getProject();
    }

    @Override
    public ITmfProjectModelElement getParent() {
        return fParent;
    }

    @Override
    public boolean hasChildren() {
        return fChildren.size() > 0;
    }

    @Override
    public List<ITmfProjectModelElement> getChildren() {
        return fChildren;
    }

    @Override
    public void addChild(ITmfProjectModelElement child) {
        fChildren.add(child);
    }

    @Override
    public void removeChild(ITmfProjectModelElement child) {
        fChildren.remove(child);
    }

    @Override
    public void refresh() {
        // make sure the model is updated in the current thread
        refreshChildren();

        Display.getDefault().asyncExec(new Runnable(){
            @Override
            public void run() {
                IWorkbench wb = PlatformUI.getWorkbench();
                IWorkbenchWindow wbWindow = wb.getActiveWorkbenchWindow();
                if (wbWindow == null) {
                    return;
                }
                IWorkbenchPage activePage = wbWindow.getActivePage();
                if (activePage == null) {
                    return;
                }

                for (IViewReference viewReference : activePage.getViewReferences()) {
                    IViewPart viewPart = viewReference.getView(false);
                    if (viewPart instanceof CommonNavigator) {
                        CommonViewer commonViewer = ((CommonNavigator) viewPart).getCommonViewer();
                        Object element = TmfProjectModelElement.this;
                        if (element instanceof TmfProjectElement) {
                            // for the project element the viewer uses the IProject resource
                            element = getResource();
                        }
                        commonViewer.refresh(element);
                    }
                }
            }});
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fPath == null) ? 0 : fPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof TmfProjectModelElement)) {
            return false;
        }
        TmfProjectModelElement element = (TmfProjectModelElement) other;
        return element.fPath.equals(fPath);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Refresh the children of this model element, adding new children and
     * removing dangling children as necessary. The remaining children should
     * also refresh their own children sub-tree.
     */
    void refreshChildren() {
        // Sub-classes may override this method as needed
    }

    /**
     * Returns the trace specific supplementary folder under the project's
     * supplementary folder. The returned folder and its parent folders may not
     * exist.
     *
     * @param supplFolderPath
     *            folder path relative to the project's supplementary folder
     * @return the trace specific supplementary folder
     */
    public IFolder getTraceSupplementaryFolder(String supplFolderPath) {
        TmfProjectElement project = getProject();
        IProject projectResource = project.getResource();
        IFolder supplFolderParent = projectResource.getFolder(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME);
        IFolder folder = supplFolderParent.getFolder(supplFolderPath);
        return folder;
    }

    /**
     * Returns the trace specific supplementary folder under the project's
     * supplementary folder. Its parent folders will be created if they don't
     * exist. If createFolder is true, the returned folder will be created,
     * otherwise it may not exist.
     *
     * @param supplFolderPath
     *            folder path relative to the project's supplementary folder
     * @param createFolder
     *            if true, the returned folder will be created
     * @return the trace specific supplementary folder
     * @since 3.0
     */
    public IFolder prepareTraceSupplementaryFolder(String supplFolderPath, boolean createFolder) {
        IFolder folder = getTraceSupplementaryFolder(supplFolderPath);
        try {
            if (createFolder) {
                TraceUtils.createFolder(folder, new NullProgressMonitor());
            } else {
                TraceUtils.createFolder((IFolder) folder.getParent(), new NullProgressMonitor());
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating supplementary folder " + folder.getFullPath(), e); //$NON-NLS-1$
        }
        return folder;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + getPath() + ')';
    }

}
