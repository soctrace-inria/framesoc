/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.views.synchronization;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import fr.inria.linuxtools.tmf.core.signal.TmfSignalHandler;
import fr.inria.linuxtools.tmf.core.signal.TmfTraceSynchronizedSignal;
import fr.inria.linuxtools.tmf.core.synchronization.SynchronizationAlgorithm;
import fr.inria.linuxtools.tmf.ui.views.TmfView;

/**
 * Small view to display statistics about a synchronization
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfSynchronizationView extends TmfView {

    /**
     * The ID corresponds to the package in which this class is embedded.
     */
    public static final String ID = "fr.inria.linuxtools.tmf.ui.views.synchronization"; //$NON-NLS-1$

    /**
     * The view name.
     */
    public static final String TMF_SYNCHRONIZATION_VIEW = "SynchronizationView"; //$NON-NLS-1$

    /**
     * The synchronization algorithm to display stats for
     */
    private SynchronizationAlgorithm fAlgoSync;

    private Tree fTree;

    /**
     * Default constructor
     */
    public TmfSynchronizationView() {
        super(TMF_SYNCHRONIZATION_VIEW);
    }

    @Override
    public void createPartControl(Composite parent) {
        fTree = new Tree(parent, SWT.NONE);
        TreeColumn nameCol = new TreeColumn(fTree, SWT.NONE, 0);
        TreeColumn valueCol = new TreeColumn(fTree, SWT.NONE, 1);
        nameCol.setText(Messages.TmfSynchronizationView_NameColumn);
        valueCol.setText(Messages.TmfSynchronizationView_ValueColumn);

        fTree.setItemCount(0);

        fTree.setHeaderVisible(true);
        nameCol.pack();
        valueCol.pack();

    }

    private void updateTable() {
        fTree.setItemCount(0);
        if (fAlgoSync == null) {
            return;
        }

        for (Map.Entry<String, Map<String, Object>> entry : fAlgoSync.getStats().entrySet()) {
            TreeItem item = new TreeItem(fTree, SWT.NONE);
            item.setText(0, entry.getKey().toString());
            item.setText(1, entry.getValue().toString());

            for (Map.Entry<String, Object> subentry : entry.getValue().entrySet()) {
                TreeItem subitem = new TreeItem(item, SWT.NONE);
                subitem.setText(0, subentry.getKey().toString());
                subitem.setText(1, subentry.getValue().toString());
            }
        }

        /* Expand the tree items */
        for (int i = 0; i < fTree.getItemCount(); i++) {
            fTree.getItem(i).setExpanded(true);
        }

        for (TreeColumn column : fTree.getColumns()) {
            column.pack();
        }
    }

    @Override
    public void setFocus() {
        fTree.setFocus();
    }

    /**
     * Handler called when traces are synchronized
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceSynchronized(TmfTraceSynchronizedSignal signal) {
        if (signal.getSyncAlgo() != fAlgoSync) {
            fAlgoSync = signal.getSyncAlgo();
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    updateTable();
                }
            });
        }
    }
}
