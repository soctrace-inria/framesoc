/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.lib.model.utils;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;

/**
 * Number format for timestamps.
 * 
 * Note, in all the methods, the position parameter is ignored.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TimestampFormat extends NumberFormat {

	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = -5615549237196509700L;

	/**
	 * Decimal format without exponent part
	 */
	private final DecimalFormat noExpFormat = new DecimalFormat("###.#");

	/**
	 * Decimal format with exponent part
	 */
	private final DecimalFormat expFormat = new DecimalFormat("###.#E0");

	/**
	 * Maximum number of decimal digits
	 */
	private static final int MAX_DECIMALS = 9;

	/**
	 * Number of fraction digits
	 */
	private int decimals = 3;

	/**
	 * Time unit
	 */
	private TimeUnit unit;

	/**
	 * Create a timestamp format with a <code>TimeUnit.UNKNOWN</code> time unit.
	 */
	public TimestampFormat() {
		this.unit = TimeUnit.UNKNOWN;
	}

	/**
	 * Create a timestamp format with the passed time unit
	 * 
	 * @param unit
	 *            time unit
	 */
	public TimestampFormat(TimeUnit unit) {
		this.unit = unit;
	}

	/**
	 * @param unit
	 *            the time unit to set
	 */
	public void setTimeUnit(TimeUnit unit) {
		this.unit = unit;
	}

	/**
	 * @return the time unit
	 */
	public TimeUnit getTimeUnit() {
		return unit;
	}

	/**
	 * Note: Ignoring parameter pos.
	 */
	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {

		switch (unit) {
		case UNKNOWN:
		case CYCLE:
		case TICK:
			expFormat.setMaximumIntegerDigits(3);
			noExpFormat.setMaximumFractionDigits(1);
			toAppendTo.append(expFormat.format(number));
			return toAppendTo;
		default:
			break;
		}

		return formatCompactFix(number, toAppendTo);
	}

	private StringBuffer formatCompact(double number, StringBuffer toAppendTo) {
		// find the lowest exponent in engineering notation
		int eng = 0;
		Double tmp = number;
		while (tmp.longValue() > 1000) {
			tmp /= 1000.0;
			eng++;
		}
		// compute the real exponent, when expressing the number in seconds
		int realExp = eng * 3 + unit.getInt();
		if (realExp > 0) {
			// number is more than seconds
			tmp *= Math.pow(10, realExp);
			if (realExp < 3) {
				noExpFormat.setMaximumFractionDigits(decimals);
				toAppendTo.append(noExpFormat.format(tmp));
			} else {
				expFormat.setMaximumFractionDigits(Math.max(1, decimals - 2));
				toAppendTo.append(expFormat.format(tmp));
			}
			toAppendTo.append(" s");
		} else {
			// number is seconds or less
			noExpFormat.setMaximumFractionDigits(decimals);
			toAppendTo.append(noExpFormat.format(tmp));
			toAppendTo.append(" ");
			toAppendTo.append(TimeUnit.getLabel(realExp));
		}
		return toAppendTo;
	}

	/**
	 * Note: Ignoring parameter pos.
	 */
	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		return format(Double.valueOf(number), toAppendTo, pos);
	}

	/**
	 * Note: Ignoring parameter pos.
	 */
	@Override
	public Number parse(String text, ParsePosition pos) {
		return Double.valueOf(text);
	}

	/**
	 * Computes the good number of decimal digits to see a difference between numbers contained
	 * between the two numbers passed. This number is limited at {@value #MAX_DECIMALS}.
	 * 
	 * @param t1
	 *            lowest displayed timestamp
	 * @param t2
	 *            highest displayed timestamp
	 */
	public void setContext(long t1, long t2) {
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		decimals = 1;
		for (; decimals < MAX_DECIMALS; decimals++) {
			sb1.setLength(0);
			sb2.setLength(0);
			formatCompact(t1, sb1);
			formatCompact(t2, sb2);
			if (!sb1.toString().equals(sb2.toString()))
				break;
		}
		decimals = Math.min(decimals + 1, MAX_DECIMALS);
	}

	/*
	 * EXPERIMENTAL
	 */

	private int eng = -1;

	public void setFixContext(long t1, long t2) {
		int e1 = getEngExp(t1);
		int e2 = getEngExp(t2);
		eng = Math.max(e1, e2);

		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		decimals = 1;
		for (; decimals < MAX_DECIMALS; decimals++) {
			sb1.setLength(0);
			sb2.setLength(0);
			formatCompactFix(t1, sb1);
			formatCompactFix(t2, sb2);
			if (!sb1.toString().equals(sb2.toString()))
				break;
		}
		decimals = Math.min(decimals + 1, MAX_DECIMALS);
	}

	private int getEngExp(long number) {
		int eng = 0;
		while (number > 1000) {
			number /= 1000.0;
			eng++;
		}
		return eng;
	}

	private StringBuffer formatCompactFix(double number, StringBuffer toAppendTo) {

		// compute an exponent in engineering notation, or use the one set by the context
		// transforming the number accordingly
		int usedEng = 0;
		Double tmp = number;
		if (eng == -1) {
			// a fixed context has not been set, find the lowest exponent in engineering notation
			while (tmp.longValue() > 1000) {
				tmp /= 1000.0;
				usedEng++;
			}
		} else {
			// a fixed context has been set, divide by 1000 according to the context
			for (int i = 0; i < eng; i++) {
				tmp /= 1000.0;
			}
			usedEng = eng;
		}

		// compute the real exponent, when expressing the number in seconds
		int realExp = usedEng * 3 + unit.getInt();
		if (realExp > 0) {
			// number is more than seconds
			tmp *= Math.pow(10, realExp);
			if (realExp < 3) {
				noExpFormat.setMaximumFractionDigits(decimals);
				toAppendTo.append(noExpFormat.format(tmp));
			} else {
				expFormat.setMaximumFractionDigits(Math.max(1, decimals - 2));
				toAppendTo.append(expFormat.format(tmp));
			}
			toAppendTo.append(" s");
		} else {
			// number is seconds or less
			noExpFormat.setMaximumFractionDigits(decimals);
			toAppendTo.append(noExpFormat.format(tmp));
			toAppendTo.append(" ");
			toAppendTo.append(TimeUnit.getLabel(realExp));
		}
		return toAppendTo;
	}

	/**
	 * Compute a GradDescriptor to be used in timebar displaying.
	 * 
	 * This method implements the following algorithm.
	 * 
	 * <pre>
	 * Input: t0, t1, # of ticks hint (N) 
	 * Output: first tick (t0') and delta (D) 
	 * Algorithm:
	 * - compute step: (t1-t0)/(N+1) 
	 * - let D be the most significant digit in step 
	 * - in t0, consider only the digit up to the one in D position, obtaining t0' (rounded) 
	 * - the first tick is (t0')*10^(position of D in step) if this is bigger than t0,
	 *   otherwise we sum D*10^(position of D in step)
	 * - to get the others, always sum D*10^(position of D in step)
	 * 
	 * E.g.: 
	 * - t0: 5129, t1: 7259, N: 9
	 * - step: 213 
	 * - D = 2 (2|13) 
	 * - position of D in step: 2nd position
	 * - t0'=51 (51|29) 
	 * - first tick: 
	 *   - 51 * 10^2 = 5100 is less than t0 so we add 2 * 10^2: 5300
	 * - second tick: 5300 * 10^2 = 5500 
	 * - other ticks: 5700, 5900, ....
	 * 
	 * </pre>
	 * 
	 * @param t0
	 *            minimum value
	 * @param t1
	 *            max value
	 * @param numberOfTicksHint
	 *            hint on the desired number of ticks
	 * @return
	 */
	public TickDescriptor getTickDescriptor(long t0, long t1, int numberOfTicksHint) {
		TickDescriptor des = new TickDescriptor();

		des.delta = (t1 - t0) / (numberOfTicksHint + 1);
		des.first = t0;

		int exp = 0;
		long step = des.delta;
		while (step > 0) {
			step /= 10;
			exp++;
		}
		exp--;
		long factor = (long) Math.pow(10, exp);

		des.delta /= factor;
		des.delta *= factor;
		des.first /= factor;
		des.first *= factor;
		if (des.first < t0) {
			des.first += des.delta;
		}

		return des;
	}

	/**
	 * Tick Descriptor for time bar displaying.
	 */
	public static class TickDescriptor {

		/**
		 * First timestamp to display
		 */
		public long first;

		/**
		 * Delta between timestamps
		 */
		public long delta;
	}

}
