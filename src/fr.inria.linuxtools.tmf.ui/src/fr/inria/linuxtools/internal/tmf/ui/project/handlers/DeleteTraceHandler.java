/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Close editors to release resources
 *   Geneviève Bastien - Moved the delete code to element model's classes
 *******************************************************************************/

package fr.inria.linuxtools.internal.tmf.ui.project.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import fr.inria.linuxtools.internal.tmf.ui.Activator;
import fr.inria.linuxtools.tmf.ui.project.model.TmfTraceElement;

/**
 * <b><u>DeleteTraceHandler</u></b>
 * <p>
 */
public class DeleteTraceHandler extends AbstractHandler {

    private TreeSelection fSelection = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return false;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return false;
        }
        ISelection selection = selectionProvider.getSelection();

        // Make sure selection contains only traces
        fSelection = null;
        if (selection instanceof TreeSelection) {
            fSelection = (TreeSelection) selection;
            Iterator<Object> iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (!(element instanceof TmfTraceElement)) {
                    return false;
                }
            }
        }

        // If we get here, either nothing is selected or everything is a trace
        return !selection.isEmpty();
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Confirm the operation
        Shell shell = window.getShell();
        MessageBox confirmOperation = new MessageBox(shell, SWT.ICON_QUESTION | SWT.CANCEL | SWT.OK);
        confirmOperation.setText(Messages.DeleteDialog_Title);
        confirmOperation.setMessage(Messages.DeleteTraceHandler_Message);
        if (confirmOperation.open() != SWT.OK) {
            return null;
        }

        Iterator<Object> iterator = fSelection.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof TmfTraceElement) {
                final TmfTraceElement trace = (TmfTraceElement) element;
                try {
                    trace.delete(null);
                } catch (final CoreException e) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                            mb.setText(Messages.DeleteTraceHandler_Error + ' ' + trace.getName());
                            mb.setMessage(e.getMessage());
                            mb.open();
                        }
                    });
                    Activator.getDefault().logError("Error deleting trace: " + trace.getName(), e); //$NON-NLS-1$
                }
            }
        }

        return null;
    }
}
