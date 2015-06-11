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
package fr.inria.soctrace.test.junit.lib.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.TimestampFormat;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TimestampFormatTest {
	
	private char decimalSeparator;

	private static class FormattedNumber<T> {
		public T num;
		public String str;
		public FormattedNumber(T n, String s){
			num = n;
			str = s;
		}
	}
	
	public TimestampFormatTest() {
		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
		DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
		decimalSeparator = symbols.getDecimalSeparator();
	}
	
	@Test 
	public void testFormatLongNs() {
	
		
		TimeUnit ns = TimeUnit.NANOSECONDS;
		List<FormattedNumber<Long>> list = new ArrayList<>();
		list.add(new FormattedNumber<Long>(111L, "111 ns"));
		list.add(new FormattedNumber<Long>(1111L, "1" + decimalSeparator + "111 us"));
		list.add(new FormattedNumber<Long>(11111L, "11" + decimalSeparator + "111 us"));
		list.add(new FormattedNumber<Long>(111111L, "111" + decimalSeparator + "111 us"));
		list.add(new FormattedNumber<Long>(1111111L, "1" + decimalSeparator + "111 ms"));
		list.add(new FormattedNumber<Long>(11111111L, "11" + decimalSeparator + "111 ms"));
		list.add(new FormattedNumber<Long>(111111111L, "111" + decimalSeparator + "111 ms"));
		list.add(new FormattedNumber<Long>(1111111111L, "1" + decimalSeparator + "111 s"));
		list.add(new FormattedNumber<Long>(11111111111L, "11" + decimalSeparator + "111 s"));
		list.add(new FormattedNumber<Long>(111111111111L, "111" + decimalSeparator + "111 s"));
		list.add(new FormattedNumber<Long>(1111111111111L, "1" + decimalSeparator + "111E3 s"));
		list.add(new FormattedNumber<Long>(Long.MAX_VALUE, "9" + decimalSeparator + "223E9 s"));
		
		TimestampFormat f = new TimestampFormat(ns);
		for (FormattedNumber<Long> fl : list) {
			assertEquals(fl.str, f.format(fl.num));
		}
	}
	
	@Test 
	public void testFormatLongUs() {
		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
		DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
		char decimalSeparator = symbols.getDecimalSeparator();
		
		TimeUnit us = TimeUnit.MICROSECONDS;
		List<FormattedNumber<Long>> list = new ArrayList<>();
		list.add(new FormattedNumber<Long>(111L, "111 us"));
		list.add(new FormattedNumber<Long>(1111L, "1" + decimalSeparator + "111 ms"));
		list.add(new FormattedNumber<Long>(11111L, "11" + decimalSeparator + "111 ms"));
		list.add(new FormattedNumber<Long>(111111L, "111" + decimalSeparator + "111 ms"));
		list.add(new FormattedNumber<Long>(1111111L, "1" + decimalSeparator + "111 s"));
		list.add(new FormattedNumber<Long>(11111111L, "11" + decimalSeparator + "111 s"));
		list.add(new FormattedNumber<Long>(111111111L, "111" + decimalSeparator + "111 s"));
		list.add(new FormattedNumber<Long>(1111111111L, "1" + decimalSeparator + "111E3 s"));
		list.add(new FormattedNumber<Long>(11111111111L, "11" + decimalSeparator + "11E3 s"));
		list.add(new FormattedNumber<Long>(111111111111L, "111" + decimalSeparator + "1E3 s"));
		list.add(new FormattedNumber<Long>(1111111111111L, "1" + decimalSeparator + "111E6 s"));
		list.add(new FormattedNumber<Long>(Long.MAX_VALUE, "9" + decimalSeparator + "223E12 s"));
		
		TimestampFormat f = new TimestampFormat(us);
		for (FormattedNumber<Long> fl : list) {
			assertEquals(fl.str, f.format(fl.num));
		}
	}

	@Test 
	public void testContextFormat() {
		TimeUnit us = TimeUnit.MICROSECONDS;
		TimestampFormat f = new TimestampFormat(us);
		long l1 = 1111112;
		long l2 = 1111114;
		assertEquals(f.format(l1), f.format(l2));
		f.setContext(l1, l2);
		assertNotEquals(f.format(l1), f.format(l2));
	}

}