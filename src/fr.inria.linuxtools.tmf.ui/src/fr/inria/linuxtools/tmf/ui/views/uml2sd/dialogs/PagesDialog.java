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

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;

import fr.inria.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.util.Messages;

/**
 * Class implementation of the pages dialog.<br>
 *
 * It is associated to an SDView and to a ISDAdvancedPagingProvider.<br>
 *
 * @version 1.0
 * @author sveyrier
 */
public class PagesDialog extends Dialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * viewer and provided are kept here as attributes
     */
    private ISDAdvancedPagingProvider fProvider = null;

    /** Current page */
    private TextArea fCurrentPage;

    /** Comment label */
    private Label fTotalPageComment;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param view The sequence diagram view reference
     * @param provider The paging provider reference
     */
    public PagesDialog(IViewPart view, ISDAdvancedPagingProvider provider) {
        super(view.getSite().getShell());
        fProvider = provider;
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public Control createDialogArea(Composite parent) {

        Group ret = new Group(parent, SWT.NONE);
        GridData data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        ret.setLayoutData(data);
        ret.setText(Messages.SequenceDiagram_PageNavigation);

        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        ret.setLayout(fillLayout);

        Label label = new Label(ret, SWT.NONE);
        label.setText(Messages.SequenceDiagram_CurrentPage);

        fCurrentPage = new TextArea(ret);
        fCurrentPage.setBounds(1, fProvider.pagesCount());
        fCurrentPage.setValue(fProvider.currentPage() + 1);

        fTotalPageComment = new Label(ret, SWT.NONE);
        fTotalPageComment.setAlignment(SWT.RIGHT);

        updateComments();

        getShell().setText(Messages.SequenceDiagram_SequenceDiagramPages);
        return ret;
    }

    @Override
    public void okPressed() {
        int currentPageValue = fCurrentPage.getValue() - 1;
        super.close();
        fProvider.pageNumberChanged(currentPageValue);
    }

    /**
     * Updates the comments texts.
     */
    private void updateComments() {
        int pages = Math.max(0, fProvider.pagesCount());
        StringBuffer totalPageCommentText = new StringBuffer();
        totalPageCommentText.append(Messages.SequenceDiagram_Total);
        totalPageCommentText.append(pages);
        totalPageCommentText.append(" "); //$NON-NLS-1$
        if (pages == 0) {
            totalPageCommentText.append(Messages.SequenceDiagram_pages);
        } else if (pages == 1) {
            totalPageCommentText.append(Messages.SequenceDiagram_page);
        } else {
            totalPageCommentText.append(Messages.SequenceDiagram_pages);
        }
        fTotalPageComment.setText(totalPageCommentText.toString());
    }


    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------

    /**
     * This is a Text Control that accepts only digits and ensures that bounds are respected
     */
    protected static class TextArea {
        /**
         * The text field.
         */
        private Text fText;
        /**
         * The minimum page value
         */
        private int fMin;
        /**
         * The maximum page value
         */
        private int fMax;

        /**
         * Constructor
         *
         * @param parent The paren composite
         */
        public TextArea(Composite parent) {
            fText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
            fText.setTextLimit(10);
        }

        /**
         * Sets the page value.
         *
         * @param page The page value
         */
        public void setValue(int page) {
            int value = Math.max(fMin, Math.min(fMax, page));
            fText.setText(Integer.toString(value));
        }

        /**
         * Returns the page value.
         *
         * @return the page value
         */
        public int getValue() {
            int res;
            try {
                res = Integer.parseInt(fText.getText());
            } catch (Exception e) {
                // ignored
                res = 0;
            }
            return Math.max(fMin, Math.min(fMax, res));
        }

        /**
         * Sets the minimum and maximum page values.
         *
         * @param min A minimum page value
         * @param max A maximum page value
         */
        public void setBounds(int min, int max) {
            fMin = Math.max(0, min);
            fMax = Math.max(fMin, max);
            Integer tab[] = new Integer[2];
            tab[0] = Integer.valueOf(fMin);
            tab[1] = Integer.valueOf(fMax);
            fText.setToolTipText(MessageFormat.format(Messages.SequenceDiagram_IsInBetween, (Object[]) tab));
        }
    }

}
