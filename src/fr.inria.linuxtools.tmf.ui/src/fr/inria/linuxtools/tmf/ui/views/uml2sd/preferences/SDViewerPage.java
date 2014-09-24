/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package fr.inria.linuxtools.tmf.ui.views.uml2sd.preferences;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fr.inria.linuxtools.tmf.ui.views.uml2sd.util.Messages;

/**
 * The Sequence Diagram preferences page implementation.
 *
 * @version 1.0
 * @author sveyrier
 */
public class SDViewerPage extends PreferencePage implements IWorkbenchPreferencePage, SelectionListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Temporary preferences tag
     */
    private static final String TEMP_TAG = SDViewPref.TEMP_TAG;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The preference handler used to access the PreferenceStore
     */
    private SDViewPref fPreferences = null;
    /**
     * BackGround color selector
     */
    private ColorFieldEditor fLineColor = null;
    /**
     * Foreground color selector
     */
    private ColorFieldEditor fBackGroundColor = null;
    /**
     * Font color selector
     */
    private ColorFieldEditor fTextColor = null;
    /**
     * List which display all modifiable sequence Diagram font
     */
    private List fClassItemList = null;
    /**
     * Font selector (The same is used for each modifiable font)
     */
    private FontFieldEditor fFont = null;
    /**
     * Link font when zooming selector
     */
    private BooleanFieldEditor fLink = null;
    /**
     * Enable tooltip selector
     */
    private BooleanFieldEditor fTooltip = null;
    /**
     * Do not take external time into account in the min max computation
     */
    private BooleanFieldEditor fNoExternalTime = null;
    /**
     * Use gradient color selector
     */
    private BooleanFieldEditor fUseGrad = null;
    /**
     * A button area.
     */
    private Composite fButtonArea;
    /**
     * SwimLane width selector
     */
    private IntegerFieldEditor fLifelineWidth = null;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    protected Control createContents(Composite parent) {
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        parent.setLayout(gl);
        Composite page = new Composite(parent, SWT.NONE);
        GridLayout pageLayout = new GridLayout();
        pageLayout.numColumns = 2;
        GridData pageLayoutdata = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        page.setLayoutData(pageLayoutdata);
        page.setLayout(pageLayout);

        fTooltip = new BooleanFieldEditor(ISDPreferences.PREF_TOOLTIP, Messages.SequenceDiagram_ShowTooltips, page);
        fTooltip.setPreferenceStore(fPreferences.getPreferenceStore());
        fTooltip.load();

        // link font with zoom pref
        fLink = new BooleanFieldEditor(ISDPreferences.PREF_LINK_FONT, Messages.SequenceDiagram_IncreaseFontSizeWhenZooming, page);
        fLink.setPreferenceStore(fPreferences.getPreferenceStore());
        fLink.load();

        fNoExternalTime = new BooleanFieldEditor(ISDPreferences.PREF_EXCLUDE_EXTERNAL_TIME, Messages.SequenceDiagram_ExcludeExternalTime, page);
        fNoExternalTime.setPreferenceStore(fPreferences.getPreferenceStore());
        fNoExternalTime.load();

        // use gradient color pref
        fUseGrad = new BooleanFieldEditor(ISDPreferences.PREF_USE_GRADIENT, Messages.SequenceDiagram_UseGradientColor, page);
        fUseGrad.setPreferenceStore(fPreferences.getPreferenceStore());
        fUseGrad.load();

        Label separator = new Label(page, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_NONE);
        GridData sepData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        separator.setLayoutData(sepData);

        Composite prefPage = new Composite(page, SWT.NONE);
        GridLayout prefPageLayout = new GridLayout();
        prefPage.setLayoutData(pageLayoutdata);
        prefPageLayout.numColumns = 1;
        prefPage.setLayout(prefPageLayout);

        // swimLane width pref
        fLifelineWidth = new IntegerFieldEditor(ISDPreferences.PREF_LIFELINE_WIDTH, Messages.SequenceDiagram_LifelineWidth, prefPage);
        fLifelineWidth.setPreferenceStore(fPreferences.getPreferenceStore());
        fLifelineWidth.setValidRange(119, 500);
        fLifelineWidth.load();

        // not very nice
        new Label(prefPage, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_NONE);
        new Label(prefPage, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_NONE);

        // Font list pref
        fClassItemList = new List(prefPage, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData tabItemLayoutdata = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        fClassItemList.setLayoutData(tabItemLayoutdata);

        String[] fontList2 = SDViewPref.getFontList2();
        for (int i = 0; i < fontList2.length; i++) {
            fClassItemList.add(fontList2[i]);
        }
        fClassItemList.setSelection(0);
        fClassItemList.addSelectionListener(this);
        fButtonArea = new Composite(prefPage, SWT.NONE);
        GridData tabItemLayoutdata2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL/* |GridData.GRAB_HORIZONTAL */| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        fButtonArea.setLayoutData(tabItemLayoutdata2);
        GridLayout buttonAreaLayout = new GridLayout();
        buttonAreaLayout.numColumns = 1;
        fButtonArea.setLayout(buttonAreaLayout);

        // font selector initialise for the lifeline font pref
        String[] fontList = SDViewPref.getFontList();
        fFont = new FontFieldEditor(fontList[0], "",//$NON-NLS-1$
                Messages.SequenceDiagram_AaBbYyZz, fButtonArea);
        fFont.getPreviewControl().setSize(500, 500);
        fFont.setPreferenceStore(fPreferences.getPreferenceStore());
        fFont.load();

        fBackGroundColor = new ColorFieldEditor(fontList[0] + SDViewPref.BACK_COLOR_POSTFIX, Messages.SequenceDiagram_Background, fButtonArea);
        fBackGroundColor.setPreferenceStore(fPreferences.getPreferenceStore());
        fBackGroundColor.load();

        fLineColor = new ColorFieldEditor(fontList[0] + SDViewPref.FORE_COLOR_POSTFIX, Messages.SequenceDiagram_Lines, fButtonArea);
        fLineColor.setPreferenceStore(fPreferences.getPreferenceStore());
        fLineColor.load();

        fTextColor = new ColorFieldEditor(fontList[0] + SDViewPref.TEXT_COLOR_POSTFIX, Messages.SequenceDiagram_Text, fButtonArea);
        fTextColor.setPreferenceStore(fPreferences.getPreferenceStore());
        fTextColor.load();
        swapPref(true);
        Dialog.applyDialogFont(page);

        return page;
    }

    @Override
    public void init(IWorkbench workbench) {
        fPreferences = SDViewPref.getInstance();
    }

    @Override
    protected void performApply() {
        // Store the prefrences in the PreferenceStore
        if (!fLifelineWidth.isValid()) {
            fLifelineWidth.showErrorMessage();
            return;
        }
        fFont.store();
        fBackGroundColor.store();
        fLineColor.store();
        fLink.store();
        fTooltip.store();
        fNoExternalTime.store();
        fTextColor.store();
        fUseGrad.store();
        fLifelineWidth.store();
        swapPref(false);
        // then save them in the preference file
        fPreferences.apply();
        swapPref(true);
    }

    @Override
    public boolean performOk() {
        performApply();
        return true;
    }

    @Override
    protected void performDefaults() {
        fLink.loadDefault();
        fTooltip.loadDefault();
        fNoExternalTime.loadDefault();
        fUseGrad.loadDefault();
        fLifelineWidth.loadDefault();

        // and all the fonts and colors
        // fonts and colors are stored for each time because
        // we are using only one FontFieldEditor
        Set<String> keySet = SDViewPref.getInstance().getFontPref().keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                fFont.setPreferenceName((String) prefName);
                fFont.loadDefault();
                fFont.setPreferenceName((String) prefName + TEMP_TAG);
                fFont.store();
            }
        }

        keySet = SDViewPref.getInstance().getBackColorPref().keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                fBackGroundColor.setPreferenceName((String) prefName);
                fBackGroundColor.loadDefault();
                fBackGroundColor.setPreferenceName((String) prefName + TEMP_TAG);
                fBackGroundColor.store();
            }

        }

        String[] fontList = SDViewPref.getFontList();
        fBackGroundColor.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + SDViewPref.BACK_COLOR_POSTFIX + TEMP_TAG);
        fBackGroundColor.load();

        keySet = SDViewPref.getInstance().getForeColorPref().keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                fLineColor.setPreferenceName((String) prefName);
                fLineColor.loadDefault();
                fLineColor.setPreferenceName((String) prefName + TEMP_TAG);
                fLineColor.store();
            }
        }

        fLineColor.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + SDViewPref.FORE_COLOR_POSTFIX + TEMP_TAG);
        fLineColor.load();

        keySet = SDViewPref.getInstance().getTextColorPref().keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                fTextColor.setPreferenceName((String) prefName);
                fTextColor.loadDefault();
                fTextColor.setPreferenceName((String) prefName + TEMP_TAG);
                fTextColor.store();
            }
        }
        fTextColor.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + SDViewPref.TEXT_COLOR_POSTFIX + TEMP_TAG);
        fTextColor.load();
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        // Store the past set font preference or else the
        // FontFieldEditor reassignment will make us loose the current modification
        fFont.store();
        fLineColor.store();
        fBackGroundColor.store();
        fTextColor.store();

        String[] fontList = SDViewPref.getFontList();

        // set the FontFieldEditor for the new selected graphNode font
        fFont.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + TEMP_TAG);
        fFont.load();

        fBackGroundColor.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + SDViewPref.BACK_COLOR_POSTFIX + TEMP_TAG);
        fBackGroundColor.load();

        fLineColor.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + SDViewPref.FORE_COLOR_POSTFIX + TEMP_TAG);
        fLineColor.load();

        fTextColor.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + SDViewPref.TEXT_COLOR_POSTFIX + TEMP_TAG);
        fTextColor.load();

        // No Background for message graphNodes
        if ((fontList[fClassItemList.getSelectionIndex()].equals(ISDPreferences.PREF_SYNC_MESS)) || (fontList[fClassItemList.getSelectionIndex()].equals(ISDPreferences.PREF_SYNC_MESS_RET))
                || (fontList[fClassItemList.getSelectionIndex()].equals(ISDPreferences.PREF_ASYNC_MESS)) || (fontList[fClassItemList.getSelectionIndex()].equals(ISDPreferences.PREF_ASYNC_MESS_RET))) {
            fBackGroundColor.setEnabled(false, fButtonArea);
        } else {
            fBackGroundColor.setEnabled(true, fButtonArea);
        }

        // No font used for execution occurrence and global frame
        if ((fontList[fClassItemList.getSelectionIndex()].equals(ISDPreferences.PREF_EXEC)) || (fontList[fClassItemList.getSelectionIndex()].equals(ISDPreferences.PREF_FRAME))) {
            fTextColor.setEnabled(false, fButtonArea);
        } else {
            fTextColor.setEnabled(true, fButtonArea);
        }

        if (fontList[fClassItemList.getSelectionIndex()].equals(ISDPreferences.PREF_FRAME)) {
            fFont.setEnabled(false, fButtonArea);
        } else {
            fFont.setEnabled(true, fButtonArea);
        }
    }

    /**
     * Swap viewer preferences.
     *
     * @param toTemp Switch to the temporary preferences
     */
    protected void swapPref(boolean toTemp) {
        String tag1 = "";//$NON-NLS-1$
        String tag2 = TEMP_TAG;
        if (!toTemp) {
            tag1 = TEMP_TAG;
            tag2 = "";//$NON-NLS-1$
        }
        Set<String> keySet = SDViewPref.getInstance().getFontPref().keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                fFont.setPreferenceName((String) prefName + tag1);
                fFont.load();
                fFont.setPreferenceName((String) prefName + tag2);
                fFont.store();
            }
        }

        keySet = SDViewPref.getInstance().getBackColorPref().keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                fBackGroundColor.setPreferenceName((String) prefName + tag1);
                fBackGroundColor.load();
                fBackGroundColor.setPreferenceName((String) prefName + tag2);
                fBackGroundColor.store();
            }
        }

        keySet = SDViewPref.getInstance().getForeColorPref().keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                fLineColor.setPreferenceName((String) prefName + tag1);
                fLineColor.load();
                fLineColor.setPreferenceName((String) prefName + tag2);
                fLineColor.store();
            }
        }

        keySet = SDViewPref.getInstance().getTextColorPref().keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                fTextColor.setPreferenceName((String) prefName + tag1);
                fTextColor.load();
                fTextColor.setPreferenceName((String) prefName + tag2);
                fTextColor.store();
            }
        }
        String[] fontList = SDViewPref.getFontList();
        if (toTemp) {
            // set the FontFieldEditor for the new selected graphNode font
            fFont.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + TEMP_TAG);
            fFont.load();

            fBackGroundColor.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + SDViewPref.BACK_COLOR_POSTFIX + TEMP_TAG);
            fBackGroundColor.load();

            fLineColor.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + SDViewPref.FORE_COLOR_POSTFIX + TEMP_TAG);
            fLineColor.load();

            fTextColor.setPreferenceName(fontList[fClassItemList.getSelectionIndex()] + SDViewPref.TEXT_COLOR_POSTFIX + TEMP_TAG);
            fTextColor.load();
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }
}
