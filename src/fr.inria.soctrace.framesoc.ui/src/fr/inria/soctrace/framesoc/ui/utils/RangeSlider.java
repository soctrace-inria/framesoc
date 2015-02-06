/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Laurent CARON (laurent.caron@gmail.com) - initial API and implementation
 *     Generoso Pagano - improvements (grads number displaying)
 *******************************************************************************/
package fr.inria.soctrace.framesoc.ui.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.mihalis.opal.utils.SWTGraphicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.TimestampFormat.TickDescriptor;
import fr.inria.soctrace.lib.model.utils.TimestampFormat;

/**
 * Instances of this class provide a slider with 2 buttons (min value, max value).
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER</dd>
 * <dd>HORIZONTAL</dd>
 * <dd>VERTICAL</dd> *
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * </p>
 */
public class RangeSlider extends Canvas {

	private final static Logger logger = LoggerFactory.getLogger(RangeSlider.class);

	private static final int MIN_WIDTH = 100;
	private static final int MIN_HEIGHT = 32;

	private static final int BARHEIGHT = 10;
	private static final int BOTTOM = 5;
	private static final int BARSIZE = 5;

	private static final int NO_STATUS = -1;

	/**
	 * Size in pixel of the bigger timestamp
	 */
	private static final int TIMESTAMP_MAX_SIZE = 100;

	private enum SELECTED_KNOB {
		NONE, UPPER, LOWER
	};

	private long minimum;
	private long maximum;
	private long lowerValue;
	private long upperValue;
	private final List<SelectionListener> listeners;
	private final Image slider, sliderHover, sliderDrag, sliderSelected;
	private final Image vSlider, vSliderHover, vSliderDrag, vSliderSelected;
	private int orientation;
	private long increment;
	private long pageIncrement;
	private SELECTED_KNOB lastSelected;
	private boolean dragInProgress;
	private Point coordUpper;
	private boolean upperHover;
	private Point coordLower;
	private boolean lowerHover;
	private long previousUpperValue;
	private long previousLowerValue;
	private boolean showGrads = false;
	private long cursorValue;
	private boolean mayShowTooltip;
	private IStatusLineManager statusLineManager;
	private TimeUnit unit;
	private TimestampFormat formatter = new TimestampFormat();

	/**
	 * Constructs a new instance of this class given its parent and a style value describing its
	 * behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class <code>SWT</code> which
	 * is applicable to instances of this class, or must be built by <em>bitwise OR</em>'ing
	 * together (that is, using the <code>int</code> "|" operator) two or more of those
	 * <code>SWT</code> style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * 
	 * @param parent
	 *            a composite control which will be the parent of the new instance (cannot be null)
	 * @param style
	 *            the style of control to construct
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the parent</li>
	 *                </ul>
	 * 
	 */
	public RangeSlider(final Composite parent, final int style) {
		super(parent, SWT.DOUBLE_BUFFERED
				| ((style & SWT.BORDER) == SWT.BORDER ? SWT.BORDER : SWT.NONE));
		this.unit = TimeUnit.UNKNOWN;
		this.cursorValue = 0;
		this.mayShowTooltip = false;
		this.minimum = this.lowerValue = 0;
		this.maximum = this.upperValue = 100;
		this.listeners = new ArrayList<>();
		this.increment = 1;
		this.pageIncrement = 10;
		this.lastSelected = SELECTED_KNOB.NONE;
		this.slider = new Image(getDisplay(), this.getClass().getClassLoader()
				.getResourceAsStream("images/slider-normal.png"));
		this.sliderHover = new Image(getDisplay(), this.getClass().getClassLoader()
				.getResourceAsStream("images/slider-hover.png"));
		this.sliderDrag = new Image(getDisplay(), this.getClass().getClassLoader()
				.getResourceAsStream("images/slider-drag.png"));
		this.sliderSelected = new Image(getDisplay(), this.getClass().getClassLoader()
				.getResourceAsStream("images/slider-selected.png"));

		this.vSlider = new Image(getDisplay(), this.getClass().getClassLoader()
				.getResourceAsStream("images/h-slider-normal.png"));
		this.vSliderHover = new Image(getDisplay(), this.getClass().getClassLoader()
				.getResourceAsStream("images/h-slider-hover.png"));
		this.vSliderDrag = new Image(getDisplay(), this.getClass().getClassLoader()
				.getResourceAsStream("images/h-slider-drag.png"));
		this.vSliderSelected = new Image(getDisplay(), this.getClass().getClassLoader()
				.getResourceAsStream("images/h-slider-selected.png"));

		if ((style & SWT.VERTICAL) == SWT.VERTICAL) {
			this.orientation = SWT.VERTICAL;
		} else {
			this.orientation = SWT.HORIZONTAL;
		}

		addListener(SWT.Dispose, new Listener() {

			@Override
			public void handleEvent(final Event event) {
				SWTGraphicUtil.dispose(RangeSlider.this.slider);
				SWTGraphicUtil.dispose(RangeSlider.this.sliderHover);
				SWTGraphicUtil.dispose(RangeSlider.this.sliderDrag);
				SWTGraphicUtil.dispose(RangeSlider.this.sliderSelected);

				SWTGraphicUtil.dispose(RangeSlider.this.vSlider);
				SWTGraphicUtil.dispose(RangeSlider.this.vSliderHover);
				SWTGraphicUtil.dispose(RangeSlider.this.vSliderDrag);
				SWTGraphicUtil.dispose(RangeSlider.this.vSliderSelected);
			}
		});

		addMouseListeners();
		addListener(SWT.KeyDown, new Listener() {

			@Override
			public void handleEvent(final Event event) {
				handleKeyDown(event);
			}
		});
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(final PaintEvent e) {
				drawWidget(e);
			}
		});

	}

	/**
	 * Add the mouse listeners (mouse up, mouse down, mouse move, mouse wheel)
	 */
	private void addMouseListeners() {
		addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				handleMouseDown(e);
			}
		});

		addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				handleMouseUp(e);
			}
		});

		addListener(SWT.MouseMove, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				handleMouseMove(e);
			}
		});

		addListener(SWT.MouseWheel, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				handleMouseWheel(e);
			}
		});

		addListener(SWT.MouseEnter, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				updateStatusLine(e.x);
			}
		});

		addListener(SWT.MouseExit, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				updateStatusLine(NO_STATUS);
			}
		});

	}

	/**
	 * Code executed when the mouse is down
	 * 
	 * @param e
	 *            event
	 */
	private void handleMouseDown(final Event e) {

		logger.debug("mouse down {}", this);

		if (this.upperHover) {
			logger.debug("mouse down upper value {}", this.upperValue);
			this.dragInProgress = true;
			this.lastSelected = SELECTED_KNOB.UPPER;
			this.previousUpperValue = this.upperValue;
			return;
		}

		if (this.lowerHover) {
			logger.debug("mouse down lower value {}", this.lowerValue);
			this.dragInProgress = true;
			this.lastSelected = SELECTED_KNOB.LOWER;
			this.previousLowerValue = this.lowerValue;
			return;
		}

		this.dragInProgress = false;
		this.lastSelected = SELECTED_KNOB.NONE;
	}

	/**
	 * Code executed when the mouse is up
	 * 
	 * @param e
	 *            event
	 */
	private void handleMouseUp(final Event e) {
		if (!this.dragInProgress) {
			return;
		}
		this.dragInProgress = false;
		if (!fireSelectionListeners(e)) {
			if (this.lastSelected == SELECTED_KNOB.UPPER) {
				logger.debug("mouse up upper value before {}", this.upperValue);
				this.upperValue = this.previousUpperValue;
				logger.debug("mouse up upper value after {}", this.upperValue);
			} else {
				logger.debug("mouse up lower value before {}", this.lowerValue);
				this.lowerValue = this.previousLowerValue;
				logger.debug("mouse up lower value after {}", this.lowerValue);
			}
			redraw();
		}
	}

	/**
	 * Fire all selection listeners
	 * 
	 * @param event
	 *            selection event
	 * @return <code>true</code> if no listener cancels the selection, <code>false</code> otherwise
	 */
	private boolean fireSelectionListeners(final Event event) {
		for (final SelectionListener selectionListener : this.listeners) {
			final SelectionEvent selectionEvent = new SelectionEvent(event);
			selectionListener.widgetSelected(selectionEvent);
			if (!selectionEvent.doit) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Code executed when the mouse pointer is moving
	 * 
	 * @param e
	 *            event
	 */
	private void handleMouseMove(final Event e) {
		final int x = e.x, y = e.y;
		final Image img = this.orientation == SWT.HORIZONTAL ? this.slider : this.vSlider;
		this.upperHover = x >= this.coordUpper.x && x <= this.coordUpper.x + img.getBounds().width
				&& y >= this.coordUpper.y && y <= this.coordUpper.y + img.getBounds().height;
		this.lowerHover = x >= this.coordLower.x && x <= this.coordLower.x + img.getBounds().width
				&& y >= this.coordLower.y && y <= this.coordLower.y + img.getBounds().height;

		if (this.orientation == SWT.HORIZONTAL) {
			this.cursorValue = (long) ((x - 9f) / computePixelSizeForHorizonalSlider())
					+ this.minimum;
		} else {
			this.cursorValue = (long) ((y - 9f) / computePixelSizeForHorizonalSlider())
					+ this.minimum;
		}
		if (cursorValue <= this.maximum && cursorValue >= this.minimum) {
			this.mayShowTooltip = true;
		}

		if (this.dragInProgress) {
			if (this.orientation == SWT.HORIZONTAL) {
				final long mouseValue = (long) ((x - 9f) / computePixelSizeForHorizonalSlider())
						+ this.minimum;
				logger.debug("mouse value {}", mouseValue);
				if (this.lastSelected == SELECTED_KNOB.UPPER) {
					logger.debug("upper value before {}", this.upperValue);
					this.upperValue = (long) (Math.ceil(mouseValue / this.increment) * this.increment);
					logger.debug("upper value after {}", this.upperValue);
					checkUpperValue();
				} else {
					logger.debug("lower value before {}", this.lowerValue);
					this.lowerValue = (long) (Math.ceil(mouseValue / this.increment) * this.increment);
					logger.debug("lower value after {}", this.lowerValue);
					checkLowerValue();
				}

			} else {
				final long mouseValue = (long) ((y - 9f) / computePixelSizeForVerticalSlider())
						+ this.minimum;
				if (this.lastSelected == SELECTED_KNOB.UPPER) {
					logger.debug("upper value before {}", this.upperValue);
					this.upperValue = (long) (Math.ceil(mouseValue / this.increment) * this.increment);
					logger.debug("upper value after {}", this.upperValue);
					checkUpperValue();
				} else {
					logger.debug("lower value before {}", this.lowerValue);
					this.lowerValue = (long) (Math.ceil(mouseValue / this.increment) * this.increment);
					logger.debug("lower value after {}", this.lowerValue);
					checkLowerValue();
				}

			}
			fireSelectionListeners(e);
		}

		updateStatusLine(x);
		redraw();
	}

	/**
	 * Code executed when the mouse wheel is activated
	 * 
	 * @param e
	 *            event
	 */
	private void handleMouseWheel(final Event e) {
		if (this.lastSelected == SELECTED_KNOB.NONE) {
			return;
		}
		if (this.lastSelected == SELECTED_KNOB.LOWER) {
			this.lowerValue += e.count * this.increment;
			checkLowerValue();
			redraw();
		} else {
			this.upperValue += e.count * this.increment;
			checkUpperValue();
			redraw();
		}
	}

	/**
	 * Check if the lower value is in ranges
	 */
	private void checkLowerValue() {
		logger.debug("to check: " + this.lowerValue);
		if (this.lowerValue < this.minimum) {
			this.lowerValue = this.minimum;
		}
		if (this.lowerValue > this.maximum) {
			this.lowerValue = this.maximum;
		}
		if (this.lowerValue > this.upperValue) {
			this.lowerValue = this.upperValue;
		}
		logger.debug("checked: " + this.lowerValue);
	}

	/**
	 * Check if the upper value is in ranges
	 */
	private void checkUpperValue() {
		logger.debug("to check: " + this.upperValue);
		if (this.upperValue < this.minimum) {
			this.upperValue = this.minimum;
		}
		if (this.upperValue > this.maximum) {
			this.upperValue = this.maximum;
		}
		if (this.upperValue < this.lowerValue) {
			this.upperValue = this.lowerValue;
		}
		logger.debug("checked: " + this.upperValue);
	}

	/**
	 * Draws the widget
	 * 
	 * @param e
	 *            paint event
	 */
	private void drawWidget(final PaintEvent e) {
		final Rectangle rect = this.getClientArea();
		if (rect.width == 0 || rect.height == 0) {
			return;
		}
		e.gc.setAdvanced(true);
		e.gc.setAntialias(SWT.ON);
		if (this.orientation == SWT.HORIZONTAL) {
			drawHorizontalRangeSlider(e.gc);
		} else {
			drawVerticalRangeSlider(e.gc);
		}
		if (this.mayShowTooltip) {
			// TODO: show tooltip (you have to manually print the rectangle and
			// the inner text)
			logger.debug("value under cursor: {}", cursorValue);
		}
	}

	/**
	 * Draw the range slider (horizontal)
	 * 
	 * @param gc
	 *            graphic context
	 */
	private void drawHorizontalRangeSlider(final GC gc) {
		drawBackgroundHorizontal(gc);
		drawBarsHorizontal(gc);
		this.coordUpper = drawHorizontalKnob(gc, this.upperValue - this.minimum, true);
		this.coordLower = drawHorizontalKnob(gc, this.lowerValue - this.minimum, false);
	}

	/**
	 * Draw the background
	 * 
	 * @param gc
	 *            graphic context
	 */
	private void drawBackgroundHorizontal(final GC gc) {
		final Rectangle clientArea = this.getClientArea();

		int ybar = clientArea.height - BARHEIGHT - BOTTOM;

		gc.setBackground(getBackground());
		gc.fillRectangle(clientArea);

		if (isEnabled()) {
			gc.setForeground(getForeground());
		} else {
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}
		gc.drawRoundRectangle(9, ybar, clientArea.width - 20, BARHEIGHT, 3, 3);

		final double pixelSize = computePixelSizeForHorizonalSlider();
		final int startX = (int) (pixelSize * (this.lowerValue - this.minimum));
		final int endX = (int) (pixelSize * (this.upperValue - this.minimum));
		if (isEnabled()) {
			gc.setBackground(getForeground());
		} else {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}
		gc.fillRectangle(12 + startX, ybar, endX - startX - 6, BARHEIGHT);
	}

	/**
	 * @return how many pixels corresponds to 1 point of value
	 */
	private double computePixelSizeForHorizonalSlider() {
		int width = getClientArea().width;
		double d = (width - 20f) / (this.maximum - this.minimum);
		return d;
	}

	/**
	 * Draw the bars
	 * 
	 * @param gc
	 *            graphic context
	 */
	private void drawBarsHorizontal(final GC gc) {

		if (isEnabled()) {
			gc.setForeground(getForeground());
		} else {
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}

		// to see the text
		gc.setBackground(getBackground());

		// to have the same time unit
		formatter.setContext(this.minimum, this.maximum, true);
		final int numberOfTicksHint = Math.max(getSize().x / TIMESTAMP_MAX_SIZE, 1);
		final double pixelSize = computePixelSizeForHorizonalSlider();
		TickDescriptor des = formatter.getTickDescriptor(this.minimum, this.maximum, numberOfTicksHint);
		long v = des.first;
		while (v < this.maximum) {
			final int x = (int) (9 + pixelSize * (v - this.minimum));
			if (showGrads) {
				String value = formatter.format(v);
				gc.setFont(new Font(getDisplay(), gc.getFont().getFontData()[0].getName(), 8,
						SWT.NONE));
				Point textSize = gc.textExtent(value);
				gc.drawText(value, x - textSize.x / 2, -2);
			}
			gc.drawLine(x, getClientArea().height - BARHEIGHT - BARSIZE - BOTTOM, x,
					getClientArea().height - BARHEIGHT - BOTTOM);
			v += des.delta;
		}
	}

	/**
	 * Draws an horizontal knob
	 * 
	 * @param gc
	 *            graphic context
	 * @param value
	 *            corresponding value
	 * @param upper
	 *            if <code>true</code>, draws the upper knob. If <code>false</code>, draws the lower
	 *            knob
	 * @return the coordinate of the upper left corner of the knob
	 */
	private Point drawHorizontalKnob(final GC gc, final long value, final boolean upper) {
		final double pixelSize = computePixelSizeForHorizonalSlider();
		final int x = (int) (pixelSize * value);
		Image image;
		if (upper) {
			if (this.upperHover) {
				image = this.dragInProgress ? this.sliderDrag : this.sliderHover;
			} else if (this.lastSelected == SELECTED_KNOB.UPPER) {
				image = this.sliderSelected;
			} else {
				image = this.slider;
			}
		} else {
			if (this.lowerHover) {
				image = this.dragInProgress ? this.sliderDrag : this.sliderHover;
			} else if (this.lastSelected == SELECTED_KNOB.LOWER) {
				image = this.sliderSelected;
			} else {
				image = this.slider;
			}
		}
		int yknob = getClientArea().height - BOTTOM + 1 - BARHEIGHT / 2
				- this.slider.getBounds().height / 2;
		if (isEnabled()) {
			gc.drawImage(image, x + 5, yknob);
		} else {
			final Image temp = new Image(getDisplay(), image, SWT.IMAGE_DISABLE);
			gc.drawImage(temp, x + 5, yknob);
			temp.dispose();
		}
		return new Point(x + 5, yknob);
	}

	/**
	 * Draw the range slider (vertical)
	 * 
	 * @param gc
	 *            graphic context
	 */
	private void drawVerticalRangeSlider(final GC gc) {
		drawBackgroundVertical(gc);
		drawBarsVertical(gc);
		this.coordUpper = drawVerticalKnob(gc, this.upperValue - this.minimum, true);
		this.coordLower = drawVerticalKnob(gc, this.lowerValue - this.minimum, false);
	}

	/**
	 * Draws the background
	 * 
	 * @param gc
	 *            graphic context
	 */
	private void drawBackgroundVertical(final GC gc) {
		final Rectangle clientArea = this.getClientArea();
		gc.setBackground(getBackground());
		gc.fillRectangle(clientArea);

		if (isEnabled()) {
			gc.setForeground(getForeground());
		} else {
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}
		gc.drawRoundRectangle(9, 9, clientArea.width - 20, clientArea.height - 20, 3, 3);

		final double pixelSize = computePixelSizeForVerticalSlider();
		final int startY = (int) (pixelSize * (this.lowerValue - this.minimum));
		final int endY = (int) (pixelSize * (this.upperValue - this.minimum));
		if (isEnabled()) {
			gc.setBackground(getForeground());
		} else {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}
		gc.fillRectangle(9, 12 + startY, clientArea.width - 20, endY - startY - 6);

	}

	/**
	 * @return how many pixels corresponds to 1 point of value
	 */
	private double computePixelSizeForVerticalSlider() {
		return (getClientArea().height - 20f) / (this.maximum - this.minimum);
	}

	/**
	 * Draws the bars TODO: grads
	 * 
	 * @param gc
	 *            graphic context
	 */
	private void drawBarsVertical(final GC gc) {
		final Rectangle clientArea = this.getClientArea();
		if (isEnabled()) {
			gc.setForeground(getForeground());
		} else {
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}

		final double pixelSize = computePixelSizeForVerticalSlider();
		for (int i = 1; i < 10; i++) {
			final int y = (int) (9 + pixelSize * (this.maximum - this.minimum) / 10f * i);
			gc.drawLine(4, y, 7, y);
			gc.drawLine(clientArea.width - 6, y, clientArea.width - 9, y);

		}

	}

	/**
	 * Draws a vertical knob
	 * 
	 * @param gc
	 *            graphic context
	 * @param value
	 *            corresponding value
	 * @param upper
	 *            if <code>true</code>, draws the upper knob. If <code>false</code>, draws the lower
	 *            knob
	 * @return the coordinate of the upper left corner of the knob
	 */
	private Point drawVerticalKnob(final GC gc, final long value, final boolean upper) {
		final double pixelSize = computePixelSizeForVerticalSlider();
		final int y = (int) (pixelSize * value); // XXX

		Image image;
		if (upper) {
			if (this.upperHover) {
				image = this.dragInProgress ? this.vSliderDrag : this.vSliderHover;
			} else if (this.lastSelected == SELECTED_KNOB.UPPER) {
				image = this.vSliderSelected;
			} else {
				image = this.vSlider;
			}
		} else {
			if (this.lowerHover) {
				image = this.dragInProgress ? this.vSliderDrag : this.vSliderHover;
			} else if (this.lastSelected == SELECTED_KNOB.LOWER) {
				image = this.vSliderSelected;
			} else {
				image = this.vSlider;
			}
		}

		if (isEnabled()) {
			gc.drawImage(image, getClientArea().width / 2 - 8, y + 2);
		} else {
			final Image temp = new Image(getDisplay(), image, SWT.IMAGE_DISABLE);
			gc.drawImage(temp, getClientArea().width / 2 - 8, y + 2);
			temp.dispose();

		}
		return new Point(getClientArea().width / 2 - 8, y + 2);
	}

	/**
	 * Code executed when a key is typed
	 * 
	 * @param event
	 *            event
	 */
	private void handleKeyDown(final Event event) {

		boolean needRedraw = false;

		if (this.lastSelected == SELECTED_KNOB.NONE) {
			this.lastSelected = SELECTED_KNOB.LOWER;
		}

		logger.debug("upper value before {}", this.upperValue);
		logger.debug("lower value before {}", this.lowerValue);
		switch (event.keyCode) {
		case SWT.HOME:
			if (this.lastSelected == SELECTED_KNOB.UPPER) {
				this.upperValue = this.minimum;
			} else {
				this.lowerValue = this.minimum;
			}
			needRedraw = true;
			break;
		case SWT.END:
			if (this.lastSelected == SELECTED_KNOB.UPPER) {
				this.upperValue = this.maximum;
			} else {
				this.lowerValue = this.maximum;
			}
			needRedraw = true;
			break;
		case SWT.PAGE_UP:
			if (this.lastSelected == SELECTED_KNOB.UPPER) {
				this.upperValue += this.pageIncrement;
			} else {
				this.lowerValue += this.pageIncrement;
			}
			needRedraw = true;
			break;
		case SWT.PAGE_DOWN:
			if (this.lastSelected == SELECTED_KNOB.UPPER) {
				this.upperValue -= this.pageIncrement;
			} else {
				this.lowerValue -= this.pageIncrement;
			}
			needRedraw = true;
			break;
		case SWT.ARROW_LEFT:
		case SWT.ARROW_UP:
			if (this.lastSelected == SELECTED_KNOB.UPPER) {
				this.upperValue -= this.increment;
			} else {
				this.lowerValue -= this.increment;
			}
			needRedraw = true;
			break;
		case SWT.ARROW_RIGHT:
		case SWT.ARROW_DOWN:
			if (this.lastSelected == SELECTED_KNOB.UPPER) {
				this.upperValue += this.increment;
			} else {
				this.lowerValue += this.increment;
			}
			needRedraw = true;
			break;
		}
		logger.debug("upper value after {}", this.upperValue);
		logger.debug("lower value after {}", this.lowerValue);

		if (needRedraw) {
			if (this.lastSelected == SELECTED_KNOB.UPPER) {
				checkUpperValue();
			} else {
				checkLowerValue();
			}
			redraw();
		}
	}

	/**
	 * Get the time unit
	 * 
	 * @return the time unit
	 */
	public TimeUnit getTimeUnit() {
		return unit;
	}

	/**
	 * Set the time unit
	 * 
	 * @param unit
	 *            unit to set
	 */
	public void setTimeUnit(TimeUnit unit) {
		this.unit = unit;
		this.formatter.setTimeUnit(unit);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the user changes
	 * the receiver's value, by sending it one of the messages defined in the
	 * <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the user changes the receiver's value.
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 * 
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 */
	public void addSelectionListener(final SelectionListener listener) {
		checkWidget();
		this.listeners.add(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) { // XXX
		final int width, height;
		checkWidget();
		if (this.orientation == SWT.HORIZONTAL) {
			if (wHint < MIN_WIDTH) {
				width = MIN_WIDTH;
			} else {
				width = wHint;
			}

			if (hHint < MIN_HEIGHT) {
				height = MIN_HEIGHT;
			} else {
				height = hHint;
			}
		} else {
			if (wHint < MIN_HEIGHT) {
				width = MIN_HEIGHT;
			} else {
				width = wHint;
			}

			if (hHint < MIN_WIDTH) {
				height = MIN_WIDTH;
			} else {
				height = hHint;
			}
		}

		return new Point(width, height);
	}

	/**
	 * Returns the amount that the selected receiver's value will be modified by when the up/down
	 * (or right/left) arrows are pressed.
	 * 
	 * @return the increment
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public long getIncrement() {
		checkWidget();
		return this.increment;
	}

	/**
	 * Returns the 'lower selection', which is the lower receiver's position.
	 * 
	 * @return the selection
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public long getLowerValue() {
		checkWidget();
		return this.lowerValue;
	}

	/**
	 * Returns the maximum value which the receiver will allow.
	 * 
	 * @return the maximum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public long getMaximum() {
		checkWidget();
		return this.maximum;
	}

	/**
	 * Returns the minimum value which the receiver will allow.
	 * 
	 * @return the minimum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public long getMinimum() {
		checkWidget();
		return this.minimum;
	}

	/**
	 * Returns the amount that the selected receiver's value will be modified by when the page
	 * increment/decrement areas are selected.
	 * 
	 * @return the page increment
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public long getPageIncrement() {
		checkWidget();
		return this.pageIncrement;
	}

	/**
	 * Returns the 'selection', which is an array where the first element is the lower selection,
	 * and the second element is the upper selection
	 * 
	 * @return the selection
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public long[] getSelection() {
		checkWidget();
		final long[] selection = new long[2];
		selection[0] = this.lowerValue;
		selection[1] = this.upperValue;
		return selection;
	}

	/**
	 * Returns the 'upper selection', which is the upper receiver's position.
	 * 
	 * @return the selection
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public long getUpperValue() {
		checkWidget();
		return this.upperValue;
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified when the user
	 * changes the receiver's value.
	 * 
	 * @param listener
	 *            the listener which should no longer be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 * 
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		checkWidget();
		this.listeners.remove(listener);
	}

	/**
	 * Sets the amount that the selected receiver's value will be modified by when the up/down (or
	 * right/left) arrows are pressed to the argument, which must be at least one.
	 * 
	 * @param increment
	 *            the new increment (must be greater than zero)
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public void setIncrement(final long increment) {
		checkWidget();
		this.increment = increment;
		redraw();
	}

	/**
	 * Sets the 'lower selection', which is the receiver's lower value, to the argument which must
	 * be greater than or equal to zero.
	 * 
	 * @param value
	 *            the new selection (must be zero or greater)
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public void setLowerValue(final long value) {
		checkWidget();
		if (this.minimum <= value && value <= this.maximum && value <= this.upperValue) {
			logger.debug("lower value before {}", this.lowerValue);
			this.lowerValue = value;
			logger.debug("lower value after {}", this.lowerValue);
		}
		redraw();

	}

	/**
	 * Sets the maximum value that the receiver will allow. This new value will be ignored if it is
	 * not greater than the receiver's current minimum value. If the new maximum is applied then the
	 * receiver's selection value will be adjusted if necessary to fall within its new range.
	 * 
	 * @param value
	 *            the new maximum, which must be greater than the current minimum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public void setMaximum(final long value) {
		checkWidget();
		if (this.minimum <= value) {
			this.maximum = value;
			if (this.lowerValue >= this.maximum) {
				logger.debug("lower value before {}", this.lowerValue);
				this.lowerValue = this.maximum;
				logger.debug("lower value after {}", this.lowerValue);
			}
			if (this.upperValue >= this.maximum) {
				logger.debug("upper value before {}", this.upperValue);
				this.upperValue = this.maximum;
				logger.debug("upper value after {}", this.upperValue);
			}
		}
		redraw();
	}

	/**
	 * Sets the minimum value that the receiver will allow. This new value will be ignored if it is
	 * negative or is not less than the receiver's current maximum value. If the new minimum is
	 * applied then the receiver's selection value will be adjusted if necessary to fall within its
	 * new range.
	 * 
	 * @param value
	 *            the new minimum, which must be nonnegative and less than the current maximum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public void setMinimum(final long value) {
		checkWidget();
		if (this.maximum >= value) {
			this.minimum = value;
			if (this.lowerValue <= this.minimum) {
				logger.debug("lower value before {}", this.lowerValue);
				this.lowerValue = this.minimum;
				logger.debug("lower value after {}", this.lowerValue);
			}
			if (this.upperValue <= this.minimum) {
				logger.debug("upper value before {}", this.upperValue);
				this.upperValue = this.minimum;
				logger.debug("upper value after {}", this.upperValue);
			}
		}
		redraw();
	}

	/**
	 * Sets the minimum and maximum values that the receiver will allow. This new values will be
	 * ignored if the minimum is greater than the maximum. If the new values are applied then the
	 * receiver's selection value will be adjusted if necessary to fall within its new range.
	 * 
	 * @param min
	 *            the new minimum, which must be nonnegative and less than the maximum
	 * @param max
	 *            the new maximum, which must be nonnegative and greater than the minimum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */

	public void setExtrema(final long min, final long max) {
		logger.debug("set extrema before {}", this);
		if (min < 0)
			return;
		if (max < min)
			return;
		checkWidget();
		this.minimum = min;
		this.maximum = max;
		if (this.lowerValue <= this.minimum) {
			logger.debug("lower value before {}", this.lowerValue);
			this.lowerValue = this.minimum;
			logger.debug("lower value after {}", this.lowerValue);
		}
		if (this.upperValue <= this.minimum) {
			logger.debug("upper value before {}", this.upperValue);
			this.upperValue = this.minimum;
			logger.debug("upper value after {}", this.upperValue);
		}
		if (this.lowerValue >= this.maximum) {
			logger.debug("lower value before {}", this.lowerValue);
			this.lowerValue = this.maximum;
			logger.debug("lower value after {}", this.lowerValue);
		}
		if (this.upperValue >= this.maximum) {
			logger.debug("upper value before {}", this.upperValue);
			this.upperValue = this.maximum;
			logger.debug("upper value after {}", this.upperValue);
		}
		logger.debug("set extrema after {}", this);
		redraw();
	}

	/**
	 * Sets the amount that the receiver's value will be modified by when the page
	 * increment/decrement areas are selected to the argument, which must be at least one.
	 * 
	 * @param pageIncrement
	 *            the page increment (must be greater than zero)
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public void setPageIncrement(final long pageIncrement) {
		checkWidget();
		this.pageIncrement = pageIncrement;
	}

	/**
	 * Sets the 'selection', which is the receiver's value, to the argument which must be greater
	 * than or equal to zero.
	 * 
	 * @param values
	 *            the new selection (first value is lower value, second value is upper value)
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public void setSelection(final long[] values) {
		checkWidget();
		setLowerValue(values[0]);
		setUpperValue(values[1]);
		checkUpperValue();
		checkLowerValue();
		redraw();
	}

	/**
	 * Sets the 'selection', which is the receiver's value, argument which must be greater than or
	 * equal to zero.
	 * 
	 * @param lowerValue
	 *            the new lower selection (must be zero or greater)
	 * @param upperValue
	 *            the new upper selection (must be zero or greater)
	 * @param notifyListeners
	 *            notify the selection listeners or not
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public void setSelection(final long lowerValue, final long upperValue, boolean notifyListeners) {
		checkWidget();

		if (lowerValue >= upperValue) {
			logger.error("Invalid selection. Lower: " + lowerValue + ", Upper: " + upperValue);
			return;
		}

		if (this.minimum <= lowerValue && lowerValue <= this.maximum && this.minimum <= upperValue
				&& upperValue <= this.maximum) {
			logger.debug("upper value before {}", this.upperValue);
			logger.debug("lower value before {}", this.lowerValue);
			this.lowerValue = lowerValue;
			this.upperValue = upperValue;
			logger.debug("upper value after {}", this.upperValue);
			logger.debug("lower value after {}", this.lowerValue);
		} else {
			logger.error("Invalid selection. Lower: " + lowerValue + ", Upper: " + upperValue);
			return;
		}

		redraw();

		if (notifyListeners) {
			Event e = new Event();
			e.doit = true;
			e.widget = this;
			fireSelectionListeners(e);
		}
	}

	/**
	 * Sets the 'upper selection', which is the upper receiver's value, argument which must be
	 * greater than or equal to zero.
	 * 
	 * @param value
	 *            the new selection (must be zero or greater)
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public void setUpperValue(final long value) {
		checkWidget();
		if (this.minimum <= value && value <= this.maximum && value >= this.lowerValue) {
			logger.debug("upper value before {}", this.upperValue);
			this.upperValue = value;
			logger.debug("upper value after {}", this.upperValue);
		}
		redraw();
	}

	/**
	 * XXX only implemented for HORIZONTAL slider
	 * 
	 * @return the showGrads
	 */
	public boolean isShowGrads() {
		return showGrads;
	}

	/**
	 * XXX only implemented for HORIZONTAL slider
	 * 
	 * @param showGrads
	 *            the showGrads to set
	 */
	public void setShowGrads(boolean showGrads) {
		this.showGrads = showGrads;
	}

	@Override
	public String toString() {
		return "RangeSlider [minimum=" + minimum + ", maximum=" + maximum + ", lowerValue="
				+ lowerValue + ", upperValue=" + upperValue + "]";
	}

	public void setStatusLineManager(IStatusLineManager manager) {
		if (statusLineManager != null && manager == null) {
			statusLineManager.setMessage(""); //$NON-NLS-1$
		}
		statusLineManager = manager;
	}

	private void updateStatusLine(int x) {
		if (statusLineManager == null) {
			return;
		}

		if (x == NO_STATUS) {
			statusLineManager.setMessage("");
			return;
		}

		StringBuilder message = new StringBuilder();
		if (!dragInProgress) {
			final long mouseValue = (long) ((x - 9f) / computePixelSizeForHorizonalSlider())
					+ this.minimum;
			message.append("T: "); //$NON-NLS-1$
			message.append(formatter.format(mouseValue));
			message.append("     ");
		}
		message.append("T1: "); //$NON-NLS-1$
		message.append(formatter.format(this.lowerValue));
		message.append("     T2: "); //$NON-NLS-1$
		message.append(formatter.format(this.upperValue));
		message.append("     \u0394: "); //$NON-NLS-1$
		message.append(formatter.format(Math.abs(this.upperValue - this.lowerValue)));
		statusLineManager.setMessage(message.toString());
	}

}
