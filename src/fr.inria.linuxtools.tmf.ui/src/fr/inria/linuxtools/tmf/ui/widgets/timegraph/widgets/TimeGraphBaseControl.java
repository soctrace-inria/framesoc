/*****************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation, Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Intel Corporation - Initial API and implementation
 *   Ruslan A. Scherbakov, Intel - Initial API and implementation
 *   Alvaro Sanchez-Leon - Updated for TMF
 *   Patrick Tasse - Refactoring
 *****************************************************************************/

package fr.inria.linuxtools.tmf.ui.widgets.timegraph.widgets;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Rectangle;


/**
 * Base control abstract class for the time graph widget
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public abstract class TimeGraphBaseControl extends Canvas {

    /** Default left margin size */
    public static final int MARGIN = 4;

    /** Default expanded size */
    public static final int EXPAND_SIZE = 9; // the [+] or [-] control size

    /** Default size of the right margin */
    public static final int RIGHT_MARGIN = 1; // 1 pixels less to make sure end time is visible

    /** Default size for small icons */
    public static final int SMALL_ICON_SIZE = 16;

    /** Color scheme */
    protected TimeGraphColorFxScheme fColorScheme;

    /** Font size */
    private int fFontHeight = 0;

    /**
     * Monitor if a change has taken place (avoid useless redrawing)
     */
    protected boolean hasChanged = true;

    /**
     * @return the haschanged flag
     */
    public boolean isHasChanged() {
        return hasChanged;
    }

    /**
     * Set the value of hasChanged flag
     *
     * @param hasChanged
     *            the new value assigned to the flag
     */
    public void setHasChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    /**
     * Standard constructor
     *
     * @param colorScheme
     *            The color scheme to use
     */
    public TimeGraphBaseControl(TimeGraphColorFxScheme colorScheme) {
        super();
        fColorScheme = colorScheme;
        //addPaintListener(this);
    }
/*
    @Override
    public void paintControl(PaintEvent e) {
        if (e.getSource() != this) {
            return;
        }
        fFontHeight = e.gc.getFontMetrics().getHeight();
        Rectangle bound = new Rectangle(0, 0, (int) getWidth(), (int) getHeight());
        if (!bound.isEmpty()) {
            org.eclipse.swt.graphics.Color colBackup = e.gc.getBackground();
            paint(bound);
            e.gc.setBackground(colBackup);
        }
    }*/

    /**
     * Retrieve the color scheme
     *
     * @return The color scheme
     *
     * @since 2.0
     */
    public TimeGraphColorFxScheme getColorScheme() {
        return fColorScheme;
    }

    /**
     * Retrieve the color scheme
     *
     * @param idx the identity of the wanted color
     *
     * @return The java fx color
     *
     * @framesoc
     */
    public Color getColorScheme(int idx) {
        org.eclipse.swt.graphics.Color swtColor = fColorScheme.getColor(idx);
        return Color.rgb(swtColor.getRed(), swtColor.getGreen(), swtColor.getBlue());
    }

    /**
     * Retrieve the current font's height
     *
     * @return The height
     */
    public int getFontHeight() {
        return fFontHeight;
    }

    /**
     * Redraw the view
     */
    public void redraw() {
        Rectangle bound = new Rectangle(0, 0, (int) getWidth(), (int) getHeight());
        if (!bound.isEmpty()) {
            paint(bound);
        }
    }

    abstract void paint(Rectangle bound);

    /**
     * @Framesoc
     *
     * @param bound
     * @param e
     * @param fullHeight
     *            should the snapshot take the whole height of the gantt or only
     *            what is displayed
     */
    void takeSnapshot(Rectangle bound, boolean fullHeight) {
        paint(bound);
    }

    /**
     * @Framesoc Take a snapshot of the current view
     *
     * @param e
     *            PaintEvent containing the info to take the snapshot
     * @param fullHeight
     *            should the snapshot take the whole height of the gantt or only
     *            what is displayed
     */
    public void takeSnapshot(PaintEvent e, boolean fullHeight) {
        Rectangle bound = new Rectangle(e.x, e.y, e.width, e.height);
        if (!bound.isEmpty()) {
            takeSnapshot(bound, fullHeight);
        }
    }
}
