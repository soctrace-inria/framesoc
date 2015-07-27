/*****************************************************************************
 * Copyright (c) 2008, 2013 Intel Corporation, Ericsson
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

import org.eclipse.swt.SWT;
import javafx.scene.paint.Color;

/**
 * Color theme used by the timegraph view
 *
 * @version 1.0
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TimeGraphColorFxScheme extends TimeGraphColorScheme {



    interface IColorProvider {
        org.eclipse.swt.graphics.Color get();
    }

    static class SysCol implements IColorProvider {
        private int syscol;

        SysCol(int syscol) {
            this.syscol = syscol;
        }

        @Override
        public org.eclipse.swt.graphics.Color get() {
            return Utils.getSysColor(syscol);
        }
    }



    static class RGB implements IColorProvider {
        private int r;
        private int g;
        private int b;

        RGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public org.eclipse.swt.graphics.Color get() {
            return new org.eclipse.swt.graphics.Color(null, r, g, b);
        }
    }

    static class Mix implements IColorProvider {
        private IColorProvider cp1;
        private IColorProvider cp2;
        private int w1;
        private int w2;

        Mix(IColorProvider cp1, IColorProvider cp2, int w1, int w2) {
            this.cp1 = cp1;
            this.cp2 = cp2;
            this.w1 = w1;
            this.w2 = w2;
        }

        Mix(IColorProvider cp1, IColorProvider cp2) {
            this.cp1 = cp1;
            this.cp2 = cp2;
            this.w1 = 1;
            this.w2 = 1;
        }

        @Override
        public org.eclipse.swt.graphics.Color get() {
            org.eclipse.swt.graphics.Color col1 = cp1.get();
            org.eclipse.swt.graphics.Color col2 = cp2.get();
            return Utils.mixColors(col1, col2, w1, w2);
        }
    }

    private static final IColorProvider PROVIDERS_MAP[] = {
        //
        new RGB(100, 100, 100), // UNKNOWN
        new RGB(174, 200, 124), // RUNNING
        new Mix(new SysCol(SWT.COLOR_BLUE), new SysCol(SWT.COLOR_GRAY), 1, 3), // SLEEPING
        new RGB(210, 150, 60), // WAITING
        new RGB(242, 225, 168), // BLOCKED
        new Mix(new SysCol(SWT.COLOR_RED), new SysCol(SWT.COLOR_GRAY), 1, 3), // DEADLOCK
        new RGB(200, 200, 200), // STOPPED
        new RGB(35, 107, 42), // STEEL BLUE
        new RGB(205,205,0), // DARK YELLOW
        new RGB(205, 0, 205), // MAGENTA
        new RGB(171, 130, 255), // PURPLE
        new RGB(255, 181, 197), // PINK
        new RGB(112, 219, 147), // AQUAMARINE
        new RGB(198, 226, 255), // SLATEGRAY
        new RGB(95, 158, 160), // CADET BLUE
        new RGB(107, 142, 35), // OLIVE


        //TODO: Does not seem to be used, check during clean-up
        new SysCol(SWT.COLOR_WHITE), // UNKNOWN_SEL
        new SysCol(SWT.COLOR_GREEN), // RUNNING_SEL
        new SysCol(SWT.COLOR_BLUE), // SLEEPING_SEL
        new SysCol(SWT.COLOR_CYAN), // WAITING_SEL
        new SysCol(SWT.COLOR_YELLOW), // BLOCKED_SEL
        new SysCol(SWT.COLOR_RED), // DEADLOCK_SEL
        new SysCol(SWT.COLOR_DARK_GRAY), // STOPPED_SEL
        new SysCol(SWT.COLOR_WHITE),
        new SysCol(SWT.COLOR_GREEN),
        new SysCol(SWT.COLOR_BLUE),
        new SysCol(SWT.COLOR_CYAN),
        new SysCol(SWT.COLOR_YELLOW),
        new SysCol(SWT.COLOR_RED),
        new SysCol(SWT.COLOR_DARK_GRAY),
        new SysCol(SWT.COLOR_WHITE),
        new SysCol(SWT.COLOR_GREEN),


        new SysCol(SWT.COLOR_LIST_BACKGROUND), // BACKGROUND
        new SysCol(SWT.COLOR_LIST_FOREGROUND), // FOREGROUND
        new RGB(232, 242, 254), // BACKGROUND_SEL
        new SysCol(SWT.COLOR_LIST_FOREGROUND), // FOREGROUND_SEL
        new SysCol(SWT.COLOR_WIDGET_BACKGROUND), // BACKGROUND_SEL_NOFOCUS
        new SysCol(SWT.COLOR_WIDGET_FOREGROUND), // FOREGROUND_SEL_NOFOCUS
        new SysCol(SWT.COLOR_WIDGET_BACKGROUND), // TOOL_BACKGROUND
        new SysCol(SWT.COLOR_WIDGET_DARK_SHADOW), // TOOL_FOREGROUND

        new SysCol(SWT.COLOR_GRAY), // FIX_COLOR
        new SysCol(SWT.COLOR_WHITE), // WHITE
        new SysCol(SWT.COLOR_GRAY), // GRAY
        new SysCol(SWT.COLOR_BLACK), // BLACK
        new SysCol(SWT.COLOR_DARK_GRAY), // DARK_GRAY

        new SysCol(SWT.COLOR_DARK_GRAY), // BLACK_BORDER
        new RGB(75, 115, 120), // GREEN_BORDER
        new SysCol(SWT.COLOR_DARK_BLUE), // DARK_BLUE_BORDER
        new RGB(242, 225, 168), // ORANGE_BORDER
        new RGB(210, 150, 60), // GOLD_BORDER
        new SysCol(SWT.COLOR_DARK_RED), // RED_BORDER
        new SysCol(SWT.COLOR_BLACK), // GRAY_BORDER
        new SysCol(SWT.COLOR_DARK_GRAY), // DARK_GREEN_BORDER
        new RGB(75, 115, 120), // DARK_YELLOW_BORDER
        new SysCol(SWT.COLOR_DARK_BLUE), // MAGENTA3_BORDER
        new RGB(242, 225, 168), // PURPLE1_BORDER
        new RGB(210, 150, 60), // PINK1_BORDER
        new SysCol(SWT.COLOR_DARK_RED), // AQUAMARINE_BORDER
        new SysCol(SWT.COLOR_BLACK), // LIGHT_BLUE_BORDER
        new SysCol(SWT.COLOR_DARK_GRAY), // BLUE_BORDER
        new RGB(75, 115, 120), // OLIVE_BORDER


        new SysCol(SWT.COLOR_GRAY), // MID_LINE
        new SysCol(SWT.COLOR_RED), // RED
        new SysCol(SWT.COLOR_GREEN), // GREEN
        new SysCol(SWT.COLOR_BLUE), // BLUE
        new SysCol(SWT.COLOR_YELLOW), // YELLOW
        new SysCol(SWT.COLOR_CYAN), // CYAN
        new SysCol(SWT.COLOR_MAGENTA), // MAGENTA

        new SysCol(SWT.COLOR_BLUE), // SELECTED_TIME
        new SysCol(SWT.COLOR_WIDGET_BACKGROUND), // LEGEND_BACKGROUND
        new SysCol(SWT.COLOR_WIDGET_DARK_SHADOW), // LEGEND_FOREGROUND

        new Mix(new RGB(150, 200, 240), new SysCol(SWT.COLOR_LIST_BACKGROUND)),     // GR_BACKGROUND
        new RGB(0, 0, 50),                                                          // GR_FOREGROUND
        new Mix(new RGB(150, 200, 240), new SysCol(SWT.COLOR_WHITE), 6, 1),         // GR_BACKGROUND_SEL
        new RGB(0, 0, 50),                                                          // GR_FOREGROUND_SEL
        new Mix(new RGB(150, 200, 240), new SysCol(SWT.COLOR_WHITE), 6, 1),         // GR_BACKGROUND_SEL_NOFOCUS
        new RGB(0, 0, 50),                                                          // GR_FOREGROUND_SEL_NOFOCUS

        new Mix(new SysCol(SWT.COLOR_GRAY), new SysCol(SWT.COLOR_LIST_BACKGROUND), 1, 3), // LIGHT_LINE

        new Mix(new SysCol(SWT.COLOR_GRAY), new SysCol(SWT.COLOR_LIST_BACKGROUND), 1, 6),   // BACKGROUND_NAME
        new Mix(new SysCol(SWT.COLOR_GRAY), new RGB(232, 242, 254), 1, 6),                  // BACKGROUND_NAME_SEL
        new Mix(new SysCol(SWT.COLOR_GRAY), new SysCol(SWT.COLOR_WIDGET_BACKGROUND), 1, 6), // BACKGROUND_NAME_SEL_NOFOCUS
    };

    private final org.eclipse.swt.graphics.Color fColors[];

    /**
     * Default constructor
     */
    public TimeGraphColorFxScheme() {
        fColors = new org.eclipse.swt.graphics.Color[PROVIDERS_MAP.length];
    }

    /**
     * Get the color matching the given index
     *
     * @param idx
     *            The index
     * @return The matching color
     */
    public Color getFxColor(int idx) {
        if (null == fColors[idx]) {
            if (idx >= STATES_SEL0 && idx <= STATES_SEL1) {
                org.eclipse.swt.graphics.Color col1 = getColor(idx - STATES_SEL0);
                org.eclipse.swt.graphics.Color col2 = getColor(BACKGROUND_SEL);
                fColors[idx] = Utils.mixColors(col1, col2, 3, 1);
            } else {
                fColors[idx] = PROVIDERS_MAP[idx].get();
            }
        }
        return Color.rgb(fColors[idx].getRed(),fColors[idx].getGreen(), fColors[idx].getBlue());
    }

    public Color getFxBkColorGroup(boolean selected, boolean focused) {
        if (selected && focused) {
            return getFxColor(GR_BACKGROUND_SEL);
        }
        if (selected) {
            return getFxColor(GR_BACKGROUND_SEL_NOFOCUS);
        }
        return getFxColor(GR_BACKGROUND);
    }

    public Color getBkFxColor(boolean selected, boolean focused, boolean name) {
        if (name) {
            if (selected && focused) {
                return getFxColor(BACKGROUND_NAME_SEL);
            }
            if (selected) {
                return getFxColor(BACKGROUND_NAME_SEL_NOFOCUS);
            }
            return getFxColor(BACKGROUND_NAME);
        }
        if (selected && focused) {
            return getFxColor(BACKGROUND_SEL);
        }
        if (selected) {
            return getFxColor(BACKGROUND_SEL_NOFOCUS);
        }
        return getFxColor(BACKGROUND);
    }

    public Color getFgFxColor(boolean selected, boolean focused) {
        if (selected && focused) {
            return getFxColor(FOREGROUND_SEL);
        }
        if (selected) {
            return getFxColor(FOREGROUND_SEL_NOFOCUS);
        }
        return getFxColor(FOREGROUND);
    }

    /**
     * Get the correct foreground color group
     *
     * @param selected
     *            Is the entry selected
     * @param focused
     *            Is the entry focused
     * @return The matching color
     */
    public Color getFgFxColorGroup(boolean selected, boolean focused) {
        if (selected && focused) {
            return getFxColor(GR_FOREGROUND_SEL);
        }
        if (selected) {
            return getFxColor(GR_FOREGROUND_SEL_NOFOCUS);
        }
        return getFxColor(GR_FOREGROUND);
    }
}
