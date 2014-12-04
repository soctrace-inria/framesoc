/*******************************************************************************
 * Copyright (c) 2012-2014 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.colors;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

/**
 * Framesoc color class.
 * 
 * <p>
 * Utility class able to manage a color, seen as a RGB triplet.
 * 
 * <p>
 * Internally both an SWT and an AWT colors are used. Both color objects are
 * lazily initialized on demand, based on the RGB description of the color.
 * 
 * <p>
 * Disposing an object of this class, dispose all the lazily initialized
 * internal color objects.
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocColor {

	/**
	 * Constant used to identify too light color components.
	 */
	public final static int TOO_LIGHT = 180;

	/**
	 * Framesoc color representing BLACK.
	 */
	public final static FramesocColor BLACK = new FramesocColor(0, 0, 0);

	/**
	 * Red
	 */
	public final int red;

	/**
	 * Green
	 */
	public final int green;

	/**
	 * Blue
	 */
	public final int blue;

	private org.eclipse.swt.graphics.Color swtColor = null;
	private java.awt.Color awtColor = null;

	/**
	 * Constructor
	 * 
	 * @param r
	 *            red
	 * @param g
	 *            green
	 * @param b
	 *            blue
	 */
	public FramesocColor(final int r, final int g, final int b) {
		if ((r > 255) || (r < 0) || (g > 255) || (g < 0) || (b > 255)
				|| (b < 0))
			throw new IllegalArgumentException();
		this.red = r;
		this.green = g;
		this.blue = b;
	}

	/**
	 * Get the SWT color corresponding to the RGB triplet. The SWT color is
	 * lazily initialized.
	 * 
	 * @return the SWT color
	 */
	public org.eclipse.swt.graphics.Color getSwtColor() {
		if (swtColor == null || swtColor.isDisposed()) {
			final Device device = Display.getDefault();
			swtColor = new org.eclipse.swt.graphics.Color(device, red, green,
					blue);
		}
		return swtColor;
	}

	/**
	 * Get the AWT color corresponding to the RGB triplet. The AWT color is
	 * lazily initialized.
	 * 
	 * @return the AWT color
	 */
	public java.awt.Color getAwtColor() {
		if (awtColor == null) {
			awtColor = new java.awt.Color(red, green, blue);
		}
		return awtColor;
	}

	/**
	 * Check if the color is too light.
	 * 
	 * @return true if the color is too light, false otherwise
	 */
	public boolean isTooLight() {
		if (blue > TOO_LIGHT && green > TOO_LIGHT && red > TOO_LIGHT)
			return true;
		else
			return false;
	}

	/**
	 * Dispose both the SWT and the AWT colors, if initialized.
	 */
	public void dispose() {
		if (swtColor != null) {
			swtColor.dispose();
			swtColor = null;
		}
		if (awtColor != null)
			awtColor = null;
	}

	@Override
	public String toString() {
		return "FramesocColor [r=" + red + ", g=" + green + ", b=" + blue + "]";
	}

	/**
	 * Deterministically generate a FramesocColor given a string. If
	 * deterministic generation is not possible (i.e., in case of exception), a
	 * random color is returned.
	 * 
	 * @param name
	 *            entity name
	 * @return a FramesocColor deterministically linked to the entity name
	 */
	public static FramesocColor generateFramesocColor(String name) {
		try {
			// using SHA-1: 20 bytes
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] bytes = md.digest(name.getBytes());
			ByteBuffer bb = ByteBuffer.wrap(bytes);
			// 4 bytes
			int r = bb.getInt() & 0x000000FF;
			// 4 bytes
			int g = bb.getInt() & 0x000000FF;
			// 4 bytes
			int b = bb.getInt() & 0x000000FF;
			return new FramesocColor(r, g, b);
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.getMessage());
			System.err.println("Returing a random color");
		}
		return generateRandomColor();
	}

	/**
	 * Generate a random FramesocColor.
	 * 
	 * @return a random FramesocColor.
	 */
	public static FramesocColor generateRandomColor() {
		return new FramesocColor((int) (Math.random() * 255),
				(int) (Math.random() * 255), (int) (Math.random() * 255));
	}

}
