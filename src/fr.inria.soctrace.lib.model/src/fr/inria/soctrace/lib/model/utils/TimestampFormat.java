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

	private TimeUnit unit;

	public TimestampFormat() {
		this.unit = TimeUnit.UNKNOWN;
	}

	public TimestampFormat(TimeUnit unit) {
		this.unit = unit;
	}

	public void setTimeUnit(TimeUnit unit) {
		this.unit = unit;
	}

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
			DecimalFormat df = new DecimalFormat("###.E0");
			df.setMaximumIntegerDigits(3);
			toAppendTo.append(df.format(number));
			return toAppendTo;
		default:
			break;
		}
		// return formatCompact(number, toAppendTo);
		return formatCompact(number, toAppendTo);
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
				DecimalFormat df = new DecimalFormat("###.#");
				df.setMaximumFractionDigits(1);
				toAppendTo.append(df.format(tmp));
			} else {
				DecimalFormat df = new DecimalFormat("###.#E0");
				df.setMaximumFractionDigits(2);
				df.setMaximumIntegerDigits(1);
				toAppendTo.append(df.format(tmp));
			}
			toAppendTo.append(" s");
		} else {
			// number is seconds or less
			DecimalFormat df = new DecimalFormat("###.#");
			df.setMaximumFractionDigits(1);
			toAppendTo.append(df.format(tmp));
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

	/*
	 * Test code
	 */

	// private final static long MS_IN_DAY = 86400000;
	// private final static long MS_IN_HOUR = 3600000;
	// private final static long MS_IN_MIN = 60000;
	// private final static long MS_IN_SEC = 1000;
	// private static final long NS_IN_SEC = 1000000000;
	//
	// private StringBuffer formatAbsolute(double number, StringBuffer toAppendTo) {
	//
	// Double msNumber = number * Math.pow(10, unit.getInt() + 3);
	// Double nsNumber = number * Math.pow(10, unit.getInt() + 9);
	//
	// String format = "";
	// if (msNumber >= MS_IN_DAY) {
	// long days = msNumber.longValue() / MS_IN_DAY;
	// toAppendTo.append(days);
	// toAppendTo.append("d:");
	// format = "H:m:s";
	// } else if (msNumber >= MS_IN_HOUR) {
	// format = "H:m:s";
	// } else if (msNumber >= MS_IN_MIN) {
	// format = "m:s";
	// } else if (msNumber >= MS_IN_SEC) {
	// format = "s";
	// }
	//
	// SimpleDateFormat timeFormat = new SimpleDateFormat(format);
	// timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	// toAppendTo.append(timeFormat.format(new Date(msNumber.longValue())));
	// toAppendTo.append(".");
	// toAppendTo.append(formatNs(nsNumber.longValue()));
	// toAppendTo.append(" s");
	// return toAppendTo;
	// }
	//
	// private String formatNs(long srcTime) {
	// StringBuffer str = new StringBuffer();
	// long ns = Math.abs(srcTime % NS_IN_SEC);
	// String nanos = Long.toString(ns);
	//        str.append("000000000".substring(nanos.length())); //$NON-NLS-1$
	// str.append(nanos);
	//
	// if (unit == TimeUnit.MILLISECONDS) {
	// return str.substring(0, 3);
	// } else if (unit == TimeUnit.MICROSECONDS) {
	// return str.substring(0, 6);
	// } else if (unit == TimeUnit.NANOSECONDS) {
	// return str.substring(0, 9);
	// }
	// return "";
	// }

}
