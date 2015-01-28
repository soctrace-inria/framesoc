/**
 * 
 */
package fr.inria.soctrace.test.junit.lib.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.TimestampFormat;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TimestampFormatTest {

	private static class FormattedNumber<T> {
		public T num;
		public String str;
		public FormattedNumber(T n, String s){
			num = n;
			str = s;
		}
	}
	
	@Test
	public void testFormatLongNs() {
		TimeUnit ns = TimeUnit.NANOSECONDS;
		List<FormattedNumber<Long>> list = new ArrayList<>();
		list.add(new FormattedNumber<Long>(111L, "111 ns"));
		list.add(new FormattedNumber<Long>(1111L, "1.111 us"));
		list.add(new FormattedNumber<Long>(11111L, "11.111 us"));
		list.add(new FormattedNumber<Long>(111111L, "111.111 us"));
		list.add(new FormattedNumber<Long>(1111111L, "1.111 ms"));
		list.add(new FormattedNumber<Long>(11111111L, "11.111 ms"));
		list.add(new FormattedNumber<Long>(111111111L, "111.111 ms"));
		list.add(new FormattedNumber<Long>(1111111111L, "1.111 s"));
		list.add(new FormattedNumber<Long>(11111111111L, "11.111 s"));
		list.add(new FormattedNumber<Long>(111111111111L, "111.111 s"));
		list.add(new FormattedNumber<Long>(1111111111111L, "1.11111E3 s"));
		list.add(new FormattedNumber<Long>(Long.MAX_VALUE, "9.22337E9 s"));
		
		TimestampFormat f = new TimestampFormat(ns);
		for (FormattedNumber<Long> fl : list) {
			assertEquals(fl.str, f.format(fl.num));
		}
	}
	
	@Test
	public void testFormatLongUs() {
		TimeUnit us = TimeUnit.MICROSECONDS;
		List<FormattedNumber<Long>> list = new ArrayList<>();
		list.add(new FormattedNumber<Long>(111L, "111 us"));
		list.add(new FormattedNumber<Long>(1111L, "1.111 ms"));
		list.add(new FormattedNumber<Long>(11111L, "11.111 ms"));
		list.add(new FormattedNumber<Long>(111111L, "111.111 ms"));
		list.add(new FormattedNumber<Long>(1111111L, "1.111 s"));
		list.add(new FormattedNumber<Long>(11111111L, "11.111 s"));
		list.add(new FormattedNumber<Long>(111111111L, "111.111 s"));
		list.add(new FormattedNumber<Long>(1111111111L, "1.11111E3 s"));
		list.add(new FormattedNumber<Long>(11111111111L, "11.1111E3 s"));
		list.add(new FormattedNumber<Long>(111111111111L, "111.111E3 s"));
		list.add(new FormattedNumber<Long>(1111111111111L, "1.11111E6 s"));
		list.add(new FormattedNumber<Long>(Long.MAX_VALUE, "9.22337E12 s"));
		
		TimestampFormat f = new TimestampFormat(us);
		for (FormattedNumber<Long> fl : list) {
			assertEquals(fl.str, f.format(fl.num));
		}
	}

}