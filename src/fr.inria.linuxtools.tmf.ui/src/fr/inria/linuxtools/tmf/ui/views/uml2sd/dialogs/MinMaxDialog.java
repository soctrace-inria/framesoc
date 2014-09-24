/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package fr.inria.linuxtools.tmf.ui.views.uml2sd.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import fr.inria.linuxtools.tmf.core.timestamp.TmfTimestamp;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.util.Messages;

/**
 * Dialog box for entering minimum and maximum time range for time compression bar.
 *
 * @version 1.0
 * @author sveyrier
 * @author Bernd Hufmann
 *
 */
public class MinMaxDialog extends Dialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Text field for minimum.
     */
    private Text fMinText;
    /**
     * Text field for maximum.
     */
    private Text fMaxText;
    /**
     * Text field for scale.
     */
    private Text fScaleText;
    /**
     * Text field for precision.
     */
    private Text fPrecisionText;
    /**
     * The sequence diagram widget reference.
     */
    private SDWidget fSdWidget;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Standard constructor.
     * @param shell The shell
     * @param viewer The sequence diagram widget reference.
     */
    public MinMaxDialog(Shell shell, SDWidget viewer) {
        super(shell);
        fSdWidget = viewer;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Method to create a grid data base on horizontal span.
     * @param span The horizontal span
     * @return a grid data object
     */
    protected GridData newGridData(int span) {
        GridData data = new GridData(GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = span;
        return data;
    }

    @Override
    protected Control createDialogArea(Composite p) {
        p.getShell().setText(Messages.SequenceDiagram_TimeCompressionBarConfig);
        Composite parent = (Composite) super.createDialogArea(p);

        GridLayout parentLayout = new GridLayout();
        parentLayout.numColumns = 6;
        parent.setLayout(parentLayout);

        Group g1 = new Group(parent, SWT.SHADOW_NONE);
        g1.setLayoutData(newGridData(3));
        GridLayout g1layout = new GridLayout();
        g1layout.numColumns = 3;
        g1.setLayout(g1layout);

        Label minLabel = new Label(g1, SWT.RADIO);
        minLabel.setText(Messages.SequenceDiagram_MinTime);
        minLabel.setLayoutData(newGridData(1));

        fMinText = new Text(g1, SWT.SINGLE | SWT.BORDER);
        fMinText.setLayoutData(newGridData(2));
        fMinText.setText(String.valueOf(fSdWidget.getFrame().getMinTime().getValue()));

        Label maxLabel = new Label(g1, SWT.RADIO);
        maxLabel.setText(Messages.SequenceDiagram_MaxTime);
        maxLabel.setLayoutData(newGridData(1));

        fMaxText = new Text(g1, SWT.SINGLE | SWT.BORDER);
        fMaxText.setLayoutData(newGridData(2));
        fMaxText.setText(String.valueOf(fSdWidget.getFrame().getMaxTime().getValue()));

        Label scaleLabel = new Label(g1, SWT.RADIO);
        scaleLabel.setText(Messages.SequenceDiagram_Scale);
        scaleLabel.setLayoutData(newGridData(1));

        fScaleText = new Text(g1, SWT.SINGLE | SWT.BORDER);
        fScaleText.setLayoutData(newGridData(2));
        fScaleText.setText(String.valueOf(fSdWidget.getFrame().getMinTime().getScale()));


        Label precisionLabel = new Label(g1, SWT.RADIO);
        precisionLabel.setText(Messages.SequenceDiagram_Precision);
        precisionLabel.setLayoutData(newGridData(1));

        fPrecisionText = new Text(g1, SWT.SINGLE | SWT.BORDER);
        fPrecisionText.setLayoutData(newGridData(2));
        fPrecisionText.setText(String.valueOf(fSdWidget.getFrame().getMinTime().getPrecision()));

        return parent;
    }

    @Override
    protected void okPressed() {
        long min = 0;
        long max = 0;
        int scale = 0;
        int precision = 0;
        try {
            min = Long.parseLong(fMinText.getText());
            max = Long.parseLong(fMaxText.getText());
            scale = Integer.parseInt(fScaleText.getText());
            precision = Integer.parseInt(fPrecisionText.getText());

            fSdWidget.getFrame().setMax(new TmfTimestamp(max, scale, precision));
            fSdWidget.getFrame().setMin(new TmfTimestamp(min, scale, precision));

            fSdWidget.redraw();

            super.okPressed();
        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.SequenceDiagram_Error, Messages.SequenceDiagram_InvalidRange);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        createButton(parent, IDialogConstants.CLIENT_ID, Messages.SequenceDiagram_Default, false);
        getButton(IDialogConstants.CLIENT_ID).addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fSdWidget.getFrame().resetCustomMinMax();
                fMinText.setText(String.valueOf(fSdWidget.getFrame().getMinTime().getValue()));
                fMaxText.setText(String.valueOf(fSdWidget.getFrame().getMaxTime().getValue()));
                fScaleText.setText(String.valueOf(fSdWidget.getFrame().getMinTime().getScale()));
                fPrecisionText.setText(String.valueOf(fSdWidget.getFrame().getMinTime().getPrecision()));
                fMaxText.getParent().layout(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });
    }
}
