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
 *******************************************************************************/

package fr.inria.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import fr.inria.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import fr.inria.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import fr.inria.linuxtools.tmf.ui.project.model.TmfProjectElement;
import fr.inria.linuxtools.tmf.ui.project.wizards.SelectTracesWizard;

/**
 * <b><u>SelectTracesHandler</u></b>
 * <p>
 */
public class SelectTracesHandler extends AbstractHandler {

    private TmfExperimentElement fExperiment = null;

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

        // Make sure there is only one selection and that it is an experiment
        fExperiment = null;
        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfExperimentElement) {
                fExperiment = (TmfExperimentElement) element;
            }
        }

        return (fExperiment != null);
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

        // Fire the Select Traces Wizard
        IWorkbench workbench = PlatformUI.getWorkbench();
        Shell shell = workbench.getActiveWorkbenchWindow().getShell();

        TmfExperimentFolder experiments = (TmfExperimentFolder) fExperiment.getParent();
        TmfProjectElement project = (TmfProjectElement) experiments.getParent();
        SelectTracesWizard wizard = new SelectTracesWizard(project, fExperiment);
        wizard.init(PlatformUI.getWorkbench(), null);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.open();

        return null;
    }

}
