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

package fr.inria.linuxtools.tmf.ui.views.uml2sd.core;

import java.util.Arrays;

import fr.inria.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;

/**
 * ExecutionOccurrence is the UML2 execution occurrence graphical representation. It is a BasicExecutionOccurrence on
 * which you can customize fill and/or.
 *
 * @see fr.inria.linuxtools.tmf.ui.views.uml2sd.core.Lifeline Lifeline for more event occurence details
 * @version 1.0
 * @author sveyrier
 *
 */
public class ExecutionOccurrence extends BasicExecutionOccurrence implements ITimeRange {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Set the red, green and blue value of the optional color to be used for filling the execution occurrence.
     */
    private int[] fFillRGB;
    /**
     * Set the red, green and blue value of the optional color to be used for drawing the execution occurrence
     */
    private int[] fStrokeRGB;
    /**
     * The occurrence image.
     */
    private IImage fImage;
    /**
     * The top ellipses image.
     */
    private IImage fEllipsesImage;
    /**
     *  The start time stamp.
     */
    private ITmfTimestamp fStartTime;
    /**
     * The end time stamp;
     */
    private ITmfTimestamp fEndTime;
    /**
     * Flag to indicate whether time information is available or not.
     */
    private boolean fHasTimeInfo;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void setLifeline(Lifeline theLifeline) {
        super.setLifeline(theLifeline);
        if (getLifeline() != null && fHasTimeInfo) {
            getLifeline().setTimeInfo(true);
            if (getLifeline().getFrame() != null) {
                getLifeline().getFrame().setHasTimeInfo(true);
            }
        }
    }

    /**
     * Set the red, green and blue value of the optional color to be used for filling the execution occurrence.
     *
     * @param red A value for red.
     * @param green A green value for green.
     * @param blue A value blue.
     */
    public void setFillColor(int red, int green, int blue) {
        fFillRGB = new int[3];
        fFillRGB[0] = red;
        fFillRGB[1] = green;
        fFillRGB[2] = blue;
    }

    /**
     * Set the red, green and blue value of the optional color to be used for drawing the execution occurrence
     *
     * @param red A value for red.
     * @param green A green value for green.
     * @param blue A value blue.
     */
    public void setStrokeColor(int red, int green, int blue) {
        fStrokeRGB = new int[3];
        fStrokeRGB[0] = red;
        fStrokeRGB[1] = green;
        fStrokeRGB[2] = blue;
    }

    /**
     * Set the corresponding image.
     *
     * @param image A image to set.
     */
    public void setImage(IImage image) {
        fImage = image;
    }

    /**
     * Set the top ellipses image.
     *
     * @param image A image to set.
     */
    public void setTopEllipsesImage(IImage image) {
        fEllipsesImage = image;
    }

    /**
     * Set the time when the execution occurrence starts.
     *
     * @param time the time when the execution occurrence starts
     * @since 2.0
     */
    public void setStartTime(ITmfTimestamp time) {
        fStartTime = time;
        fHasTimeInfo = true;
        if (getLifeline() != null) {
            getLifeline().setTimeInfo(true);
        }
    }

    /**
     * Set the time when the execution occurrence ends.
     *
     * @param time the time when the execution occurrence ends
     * @since 2.0
     */
    public void setEndTime(ITmfTimestamp time) {
        fEndTime = time;
        fHasTimeInfo = true;
        if (getLifeline() != null) {
            getLifeline().setTimeInfo(true);
        }
    }

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getStartTime() {
        return fStartTime;
    }

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return fEndTime;
    }

    @Override
    public boolean hasTimeInfo() {
        return fHasTimeInfo;
    }

   /**
    * @return the RGB of the occurrence filler.
    * @since 2.0
    */
    public int[] getFillRGB() {
       if (fFillRGB == null) {
           return null;
       }
       return Arrays.copyOf(fFillRGB, fFillRGB.length);
   }

    /**
     * @return the RGB of the occurrence filler.
     * @since 2.0
     */
     public int[] getStrokeRGB() {
        if (fStrokeRGB == null) {
            return null;
        }
        return Arrays.copyOf(fStrokeRGB, fStrokeRGB.length);
    }

    /**
     * @return the image.
     * @since 2.0
     */
    protected IImage getImage() {
        return fImage;
    }

    /**
     * @return the image.
     * @since 2.0
     */
    protected IImage getEllipsesImage() {
        return fEllipsesImage;
    }

    @Override
    public void draw(IGC context) {
        super.draw(context);
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        if (fImage != null) {
            context.drawImage(fImage, x + width - 4, y + height - 11, 8, 11);
        }
        if (fEllipsesImage != null) {
            context.drawImage(fEllipsesImage, x + width, y, 40, 10);
        }
    }

    @Override
    protected IColor setUnselectedFillColor(IGC context) {
        ISDPreferences pref = SDViewPref.getInstance();
        if (fFillRGB != null) {
            IColor tempFillColor = context.createColor(fFillRGB[0], fFillRGB[1], fFillRGB[2]);
            if (pref.useGradienColor()) {
                context.setGradientColor(tempFillColor);
                context.setForeground(pref.getForeGroundColor(ISDPreferences.PREF_EXEC));
                context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_FRAME));
            } else {
                context.setBackground(tempFillColor);
            }
            return tempFillColor;
        }
        return super.setUnselectedFillColor(context);
    }

    @Override
    protected IColor setUnselectedStrokeColor(IGC context) {
        if (fStrokeRGB != null) {
            IColor tempStrokeColor = context.createColor(fStrokeRGB[0], fStrokeRGB[1], fStrokeRGB[2]);
            context.setForeground(tempStrokeColor);
            return tempStrokeColor;
        }
        return super.setUnselectedStrokeColor(context);
    }

    /**
     * Sets the flag whether the frame has time info or not
     * @since 2.0
     * @param hasTimeInfo
     *          true if frame has time info else false
     */
    public void setHasTimeInfo(boolean hasTimeInfo) {
        fHasTimeInfo = hasTimeInfo;
    }
}
