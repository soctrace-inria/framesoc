/**
 * 
 */
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

	public TimestampFormat(TimeUnit unit) {
		this.unit = unit;
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

		// find the lowest exponent in engineering notation
		int eng = 0;
		Double tmp = number;
		while (tmp.longValue() > 1000) {
			tmp /= 1000.0;
			eng++;
		}
		int realExp = eng * 3 + unit.getInt();
		if (realExp > 0) {
			// number is more than seconds
			tmp *= Math.pow(10, realExp);
			if (realExp < 3) {
				DecimalFormat df = new DecimalFormat("###.#");
				toAppendTo.append(df.format(tmp));
			} else {
				DecimalFormat df = new DecimalFormat("###.#E0");
				df.setMaximumIntegerDigits(3);
				toAppendTo.append(df.format(tmp));
			}
			toAppendTo.append(" s");
		} else {
			// number is seconds or less
			DecimalFormat df = new DecimalFormat("###.#");
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

}
